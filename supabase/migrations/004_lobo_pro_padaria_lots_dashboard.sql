create or replace function public.lobo_pro_cloud_dashboard(payload jsonb default '{}'::jsonb)
returns jsonb language sql security definer set search_path = public as $$
select jsonb_build_object(
    'generated_label', to_char(now() at time zone 'America/Sao_Paulo', 'DD/MM/YYYY HH24:MI'),
    'summary', jsonb_build_object(
        'devices', (select count(*) from public.lobo_pro_devices),
        'prints_total', (select count(*) from public.lobo_pro_print_history),
        'labels_total', (select coalesce(sum(copies), 0) from public.lobo_pro_print_history),
        'checks_total', (select count(*) from public.lobo_pro_check_history)
    ),
    'recent_prints', coalesce((select jsonb_agg(jsonb_build_object(
        'product', product,
        'copies', copies,
        'printed_label', to_char(printed_at at time zone 'America/Sao_Paulo', 'DD/MM HH24:MI'),
        'expiry_label', coalesce(to_char(expiry_at at time zone 'America/Sao_Paulo', 'DD/MM/YYYY HH24:MI'), ''),
        'lote', coalesce(metadata->>'lote', ''),
        'peso_kg', coalesce(metadata->>'peso_kg', ''),
        'origem', coalesce(metadata->>'origem', ''),
        'tipo', coalesce(metadata->>'tipo', '')
    ) order by printed_at desc) from (select * from public.lobo_pro_print_history order by printed_at desc limit 30) rows), '[]'::jsonb),
    'recent_checks', coalesce((select jsonb_agg(jsonb_build_object(
        'product', product, 'status', coalesce(status, ''), 'danger', danger, 'complete', complete,
        'checked_label', to_char(checked_at at time zone 'America/Sao_Paulo', 'DD/MM HH24:MI')
    ) order by checked_at desc) from (select * from public.lobo_pro_check_history order by checked_at desc limit 20) rows), '[]'::jsonb)
);
$$;

revoke all on function public.lobo_pro_cloud_dashboard(jsonb) from public;
grant execute on function public.lobo_pro_cloud_dashboard(jsonb) to anon, authenticated;
