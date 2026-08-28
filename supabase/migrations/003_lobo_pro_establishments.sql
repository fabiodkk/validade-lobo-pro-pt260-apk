create extension if not exists pgcrypto;

create table if not exists public.lobo_pro_establishments (
    id uuid primary key default gen_random_uuid(),
    slug text not null unique,
    display_name text not null,
    address text,
    access_password_hash text not null,
    active boolean not null default true,
    created_at timestamptz not null default now()
);

alter table public.lobo_pro_devices
    add column if not exists establishment_id uuid references public.lobo_pro_establishments(id) on delete set null;
alter table public.lobo_pro_print_history
    add column if not exists establishment_id uuid references public.lobo_pro_establishments(id) on delete set null;
alter table public.lobo_pro_check_history
    add column if not exists establishment_id uuid references public.lobo_pro_establishments(id) on delete set null;

insert into public.lobo_pro_establishments (slug, display_name, address, access_password_hash)
values
    ('padaria-lobo', 'Padaria Lobo', 'Av. Mal. Floriano Peixoto, 260 - Poiares, Caraguatatuba - SP, 11673-000', extensions.crypt('lobopt260', extensions.gen_salt('bf'))),
    ('peixaria', 'Peixaria', null, extensions.crypt('pt260', extensions.gen_salt('bf'))),
    ('peixeiro', 'Peixeiro', null, extensions.crypt('pt260', extensions.gen_salt('bf')))
on conflict (slug) do nothing;

create index if not exists lobo_pro_print_establishment_idx
    on public.lobo_pro_print_history (establishment_id, printed_at desc);
create index if not exists lobo_pro_check_establishment_idx
    on public.lobo_pro_check_history (establishment_id, checked_at desc);

revoke all on table public.lobo_pro_establishments from anon, authenticated;

create or replace function public.lobo_pro_create_establishment(payload jsonb)
returns jsonb language plpgsql security definer set search_path = public as $$
declare
    admin_login text := coalesce(payload->>'admin_login', '');
    admin_password text := coalesce(payload->>'admin_password', '');
    establishment_name text := nullif(trim(payload->>'display_name'), '');
    establishment_password text := nullif(payload->>'establishment_password', '');
    establishment_slug text := lower(regexp_replace(coalesce(establishment_name, ''), '[^a-zA-Z0-9]+', '-', 'g'));
    created_establishment public.lobo_pro_establishments;
begin
    if admin_login <> 'lobo' or admin_password <> 'lobopt260' then
        raise exception 'admin credentials invalid';
    end if;
    if establishment_name is null or establishment_password is null or length(establishment_password) < 4 then
        raise exception 'display_name and a password with at least 4 characters are required';
    end if;
    if establishment_slug = '' then
        raise exception 'invalid establishment name';
    end if;
    insert into public.lobo_pro_establishments (slug, display_name, address, access_password_hash)
    values (establishment_slug, establishment_name, nullif(trim(payload->>'address'), ''), extensions.crypt(establishment_password, extensions.gen_salt('bf')))
    returning * into created_establishment;
    return jsonb_build_object('id', created_establishment.id, 'slug', created_establishment.slug, 'display_name', created_establishment.display_name);
exception
    when unique_violation then
        raise exception 'establishment already exists';
end;
$$;

create or replace function public.lobo_pro_resolve_establishment(payload jsonb)
returns uuid language sql security definer set search_path = public as $$
select id
from public.lobo_pro_establishments
where active
  and slug = lower(trim(payload->>'establishment_slug'))
    and access_password_hash = extensions.crypt(payload->>'establishment_password', access_password_hash)
limit 1;
$$;

revoke all on function public.lobo_pro_create_establishment(jsonb) from public;
revoke all on function public.lobo_pro_resolve_establishment(jsonb) from public;
grant execute on function public.lobo_pro_create_establishment(jsonb) to anon, authenticated;
grant execute on function public.lobo_pro_resolve_establishment(jsonb) to anon, authenticated;

create or replace function public.lobo_pro_record_print(payload jsonb)
returns void language plpgsql security definer set search_path = public as $$
declare
    install_id uuid := nullif(payload->>'app_install_id', '')::uuid;
    tenant_id uuid := public.lobo_pro_resolve_establishment(payload);
begin
    if tenant_id is null then
        select id into tenant_id from public.lobo_pro_establishments where slug = 'padaria-lobo' and active;
    end if;
    if install_id is null or tenant_id is null then raise exception 'valid establishment and app_install_id required'; end if;
    perform public.lobo_pro_upsert_device(payload);
    update public.lobo_pro_devices set establishment_id = tenant_id where app_install_id = install_id;
    insert into public.lobo_pro_print_history (
        app_install_id, establishment_id, printed_at, product, copies, start_at, expiry_at,
        validity_label, uses_hours, printer_name, printer_address_hash,
        printer_address_last4, source, locale, timezone, region_country,
        region_language, app_version_name, app_version_code, metadata, client_event_id
    ) values (
        install_id, tenant_id,
        coalesce(to_timestamp(nullif(payload->>'printed_at_ms', '')::numeric / 1000.0), now()),
        coalesce(nullif(payload->>'product', ''), 'Produto'),
        greatest(1, least(999, coalesce(nullif(payload->>'copies', '')::integer, 1))),
        case when coalesce(nullif(payload->>'start_at_ms', '')::numeric, 0) > 0 then to_timestamp((payload->>'start_at_ms')::numeric / 1000.0) end,
        case when coalesce(nullif(payload->>'expiry_at_ms', '')::numeric, 0) > 0 then to_timestamp((payload->>'expiry_at_ms')::numeric / 1000.0) end,
        nullif(payload->>'validity_label', ''), coalesce((payload->>'uses_hours')::boolean, false),
        nullif(payload->>'printer_name', ''), nullif(payload->>'printer_address_hash', ''),
        nullif(payload->>'printer_address_last4', ''), coalesce(nullif(payload->>'source', ''), 'android_validity_print'),
        nullif(payload->>'locale', ''), nullif(payload->>'timezone', ''), nullif(payload->>'region_country', ''),
        nullif(payload->>'region_language', ''), nullif(payload->>'app_version_name', ''),
        nullif(payload->>'app_version_code', '')::integer, coalesce(payload->'metadata', '{}'::jsonb),
        nullif(payload->>'client_event_id', '')
    ) on conflict (client_event_id) where client_event_id is not null do nothing;
end;
$$;

create or replace function public.lobo_pro_cloud_dashboard(payload jsonb default '{}'::jsonb)
returns jsonb language plpgsql security definer set search_path = public as $$
declare
    tenant_id uuid := public.lobo_pro_resolve_establishment(payload);
begin
    if tenant_id is null then
        select id into tenant_id from public.lobo_pro_establishments where slug = 'padaria-lobo' and active;
    end if;
    return jsonb_build_object(
        'generated_label', to_char(now() at time zone 'America/Sao_Paulo', 'DD/MM/YYYY HH24:MI'),
        'summary', jsonb_build_object(
            'devices', (select count(*) from public.lobo_pro_devices where establishment_id = tenant_id),
            'prints_total', (select count(*) from public.lobo_pro_print_history where establishment_id = tenant_id),
            'labels_total', (select coalesce(sum(copies), 0) from public.lobo_pro_print_history where establishment_id = tenant_id),
            'checks_total', (select count(*) from public.lobo_pro_check_history where establishment_id = tenant_id)
        ),
        'recent_prints', coalesce((select jsonb_agg(jsonb_build_object(
            'product', product, 'copies', copies, 'lote', coalesce(metadata->>'lote', ''),
            'peso_kg', coalesce(metadata->>'peso_kg', ''), 'origem', coalesce(metadata->>'origem', ''),
            'printed_label', to_char(printed_at at time zone 'America/Sao_Paulo', 'DD/MM HH24:MI'),
            'expiry_label', coalesce(to_char(expiry_at at time zone 'America/Sao_Paulo', 'DD/MM/YYYY HH24:MI'), '')
        ) order by printed_at desc) from (select * from public.lobo_pro_print_history where establishment_id = tenant_id order by printed_at desc limit 30) rows), '[]'::jsonb),
        'recent_checks', coalesce((select jsonb_agg(jsonb_build_object(
            'product', product, 'status', coalesce(status, ''), 'danger', danger, 'complete', complete,
            'checked_label', to_char(checked_at at time zone 'America/Sao_Paulo', 'DD/MM HH24:MI')
        ) order by checked_at desc) from (select * from public.lobo_pro_check_history where establishment_id = tenant_id order by checked_at desc limit 20) rows), '[]'::jsonb)
    );
end;
$$;