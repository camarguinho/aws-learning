[⬅ Sumário](../../README.md) · Trilha SAA-C03 · Diário de erros

# Diário de erros

Este é o documento que mais converte estudo em nota. Cada erro (e cada acerto
por chute) vira uma linha aqui. Revise a lista antes de cada simulado.

## Como preencher

- **Origem**: simulado, questão do banco ou laboratório.
- **Tema**: o serviço ou conceito principal.
- **Por que errei**: lacuna de conteúdo, confusão entre serviços, leitura
  apressada ou pegadinha da restrição.
- **Regra para lembrar**: uma frase que você consiga recitar (por exemplo, "CNAME
  não vale na raiz; use Alias").
- **Revisar em**: datas de revisão espaçada (1, 3, 7 e 21 dias depois).

## Padrões de erro mais comuns

| Padrão | Sintoma | Antídoto |
|---|---|---|
| Ignorar a restrição | escolher a solução mais "completa" em vez da mais barata ou simples | sublinhar a restrição principal antes de ler as opções |
| Confundir serviços parecidos | SQS/SNS/EventBridge, CloudFront/Global Accelerator, EBS/EFS | refazer as tabelas "X vs Y" de memória |
| HA vs. DR | usar Multi-AZ para falha de região | perguntar: a falha é de AZ ou de região? |
| Excesso de engenharia | escolher Kubernetes ou multi-região sem necessidade | procurar o serviço gerenciado mais simples que atende |
| Segurança frouxa | aceitar credencial fixa ou permissão ampla | preferir role, KMS CMK e menor privilégio |

## Registro

| Data | Origem | Tema | Por que errei | Regra para lembrar | Revisar em |
|---|---|---|---|---|---|
| | | | | | |
| | | | | | |
| | | | | | |

## Revisão por domínio

Depois de cada simulado, marque seu percentual por domínio:

| Simulado | Segurança | Resiliência | Performance | Custo | Total |
|---|---|---|---|---|---|
| 1 | | | | | |
| 2 | | | | | |
| 3 | | | | | |

Meta de prontidão: 80% ou mais no total e nenhum domínio abaixo de 70%
(ver o [plano de estudos](00-plano-de-estudos.md)).

---
**Anterior:** [← Laboratórios](labs.md) | [⬆ Voltar ao Sumário](../../README.md)
