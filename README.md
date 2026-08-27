# Validade Lobo Pro PT260

Aplicativo Android Pro do Validade PT260, mantido em um repositorio separado.

Este projeto nao deve compartilhar tabelas, migrations, credenciais ou artefatos com o app Validade PT260. Os dados do Lobo Pro usam o namespace `lobo_pro` no Supabase.

## Seguranca

- Nunca versionar `.env`, chaves privadas, senhas ou arquivos de assinatura.
- Usar apenas a chave anon/publica no aplicativo Android.
- Chaves de servico devem ficar somente no backend ou nas Edge Functions do Supabase.

## Banco de dados

As migrations do Lobo Pro ficam em `supabase/migrations` e usam tabelas e funcoes com prefixo `lobo_pro_`, evitando conflito com as tabelas do Validade PT260.
