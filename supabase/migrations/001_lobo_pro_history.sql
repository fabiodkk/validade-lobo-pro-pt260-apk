create extension if not exists pgcrypto;

create table if not exists public.lobo_pro_devices (
    app_install_id uuid primary key,
    user_id uuid references auth.users(id) on delete set null,
    company_name text not null default 'Padaria Lobo',
    device_name text,
    app_version_name text,
    app_version_code integer,
    metadata jsonb not null default '{}'::jsonb,
    first_seen_at timestamptz not null default now(),
    last_seen_at timestamptz not null default now()
);

create table if not exists public.lobo_pro_print_history (
    id uuid primary key default gen_random_uuid(),
    app_install_id uuid not null references public.lobo_pro_devices(app_install_id) on delete cascade,
    user_id uuid references auth.users(id) on delete set null,
    company_name text not null default 'Padaria Lobo',
    product text not null,
    copies integer not null default 1 check (copies > 0 and copies <= 999),
    printed_at timestamptz not null default now(),
    expiry_at timestamptz,
    validity_label text,
    client_event_id text,
    metadata jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now()
);

create unique index if not exists lobo_pro_print_history_event_idx
    on public.lobo_pro_print_history (client_event_id)
    where client_event_id is not null;

create index if not exists lobo_pro_print_history_user_idx
    on public.lobo_pro_print_history (user_id, printed_at desc);

create index if not exists lobo_pro_print_history_company_idx
    on public.lobo_pro_print_history (company_name, printed_at desc);

alter table public.lobo_pro_devices enable row level security;
alter table public.lobo_pro_print_history enable row level security;

revoke all on table public.lobo_pro_devices from anon, authenticated;
revoke all on table public.lobo_pro_print_history from anon, authenticated;

create or replace function public.lobo_pro_record_print(payload jsonb)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    install_id uuid := nullif(payload->>'app_install_id', '')::uuid;
    current_user_id uuid := auth.uid();
begin
    if install_id is null then
        raise exception 'app_install_id required';
    end if;

    insert into public.lobo_pro_devices (app_install_id, user_id, company_name, last_seen_at)
    values (install_id, current_user_id, coalesce(nullif(payload->>'company_name', ''), 'Padaria Lobo'), now())
    on conflict (app_install_id) do update set
        user_id = coalesce(excluded.user_id, public.lobo_pro_devices.user_id),
        company_name = excluded.company_name,
        last_seen_at = now();

    insert into public.lobo_pro_print_history (
        app_install_id, user_id, company_name, product, copies, printed_at,
        expiry_at, validity_label, client_event_id, metadata
    ) values (
        install_id,
        current_user_id,
        coalesce(nullif(payload->>'company_name', ''), 'Padaria Lobo'),
        coalesce(nullif(payload->>'product', ''), 'Produto'),
        greatest(1, least(999, coalesce(nullif(payload->>'copies', '')::integer, 1))),
        coalesce(to_timestamp(nullif(payload->>'printed_at_ms', '')::numeric / 1000.0), now()),
        case when coalesce(nullif(payload->>'expiry_at_ms', '')::numeric, 0) > 0
            then to_timestamp((payload->>'expiry_at_ms')::numeric / 1000.0)
            else null end,
        nullif(payload->>'validity_label', ''),
        nullif(payload->>'client_event_id', ''),
        coalesce(payload->'metadata', '{}'::jsonb)
    ) on conflict (client_event_id) where client_event_id is not null do nothing;
end;
$$;

revoke all on function public.lobo_pro_record_print(jsonb) from public;
grant execute on function public.lobo_pro_record_print(jsonb) to authenticated;