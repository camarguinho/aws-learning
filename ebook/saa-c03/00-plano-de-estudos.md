[⬅ Sumário](../../README.md) · Trilha SAA-C03 · Parte 0

# Plano de estudos SAA-C03

Esta trilha complementa os capítulos 0 a 18 do ebook. Os capítulos originais
ensinam serviços na prática, a partir de um e-commerce em Spring Boot. A
trilha SAA-C03 cobre o que a prova cobra e o projeto não exercita, e organiza
o estudo para você chegar à prova com segurança.

## O formato da prova

- 65 questões (50 pontuadas e 15 não pontuadas, que você não sabe quais são).
- 130 minutos.
- Nota mínima 720 de 1000.
- Só vale a nota geral; não existe nota mínima por domínio.
- Questões de múltipla escolha (uma correta) e de múltipla resposta (duas ou
  mais corretas).

| Domínio | Peso |
|---|---|
| 1. Arquiteturas seguras | 30% |
| 2. Arquiteturas resilientes | 26% |
| 3. Arquiteturas de alta performance | 24% |
| 4. Arquiteturas com custo otimizado | 20% |

Conclusão prática: segurança e resiliência juntas valem 56% da prova. Distribua
o tempo de estudo na mesma proporção.

## Como a prova pensa

A prova quase nunca pergunta "o que é o serviço X". Ela descreve um cenário e
pede a melhor solução dentro de uma restrição. As palavras-chave do enunciado
decidem a resposta:

| Se o enunciado diz... | Pense em... |
|---|---|
| "menor custo operacional", "totalmente gerenciado" | serviço gerenciado/serverless em vez de EC2 próprio |
| "mais econômico" | Spot, Savings Plans, classes S3 frias, Gateway Endpoint |
| "alta disponibilidade" | Multi-AZ |
| "recuperação de desastre", "região inteira" | Multi-Region, estratégia de DR |
| "desacoplar", "absorver picos" | SQS, SNS, EventBridge |
| "menor latência global" | CloudFront, Global Accelerator |
| "sem expor à internet" | VPC Endpoint, PrivateLink, subnet privada |
| "mínimo privilégio" | políticas IAM restritas, roles, SCPs |
| "criptografar com controle da chave" | KMS (CMK gerenciada pelo cliente) |
| "sem alterar a aplicação" | solução transparente (ex.: Storage Gateway, Aurora compatível) |

Estratégia de resposta: elimine primeiro as alternativas que violam a
restrição principal, depois escolha entre as restantes pela menor complexidade
operacional que ainda atende ao requisito.

## As 5 fases

### Fase 1: Fundamentos

Capítulos 1 a 3 do ebook (Well-Architected, IAM, VPC), mais armazenamento em
bloco e arquivo ([Capítulo S5](05-armazenamento-ebs-efs-fsx.md)). Meta: você
explica sem consultar o que é uma subnet pública, o que um Security Group
faz e como uma role é assumida.

### Fase 2: Domínio por domínio

| Domínio | Capítulos do ebook | Capítulos da trilha |
|---|---|---|
| Segurança (30%) | 2, 11 | [S3](03-seguranca-kms-waf-governanca.md) |
| Resiliência (26%) | 6, 9, 15 | [S1](01-elb-e-auto-scaling.md), [S2](02-route53-cloudfront-global-accelerator.md), [S4](04-dr-e-backup.md) |
| Performance (24%) | 4, 5, 7, 8 | [S5](05-armazenamento-ebs-efs-fsx.md), [S7](07-analytics-integracao-containers.md) |
| Custo (20%) | 16 | [S8](08-migracao-transferencia-e-custos.md) |
| Rede híbrida (transversal) | 3 | [S6](06-conectividade-hibrida.md) |

### Fase 3: Laboratórios no console

Siga os roteiros em [Laboratórios](labs.md). O LocalStack não reproduz
balanceador, Auto Scaling, CloudFront nem Route 53, que são os assuntos mais
frequentes da prova. Esses labs precisam ser feitos na conta AWS real (Free
Tier), sempre com o passo de limpeza no final.

### Fase 4: Questões de cenário

Use o [Banco de questões](questoes-de-cenario.md). Regra de ouro: para cada erro,
escreva por que a alternativa certa vence e por que cada uma das outras
falha. Esse é o exercício que mais converte estudo em nota.

### Fase 5: Simulados completos

Faça provas de 65 questões em 130 minutos, sem consulta. Aprofunde-se nos
resultados por domínio, mesmo a prova valendo só a nota geral.

## Critério de prontidão

Marque a prova quando cumprir todos estes pontos:

- Acertar 80% ou mais em pelo menos 3 simulados de qualidade, em condições de
  prova, em dias diferentes.
- Explicar o motivo de cada erro e de cada acerto duvidoso.
- Ter feito todos os labs da trilha.
- Não ter nenhum domínio abaixo de 70% nos simulados.

Passar raspando em um simulado não é segurança. Se oscilar, volte à Fase 4.

## Rotina sugerida por sessão

1. Revisão espaçada: 5 a 10 perguntas rápidas do que foi estudado nas sessões
   anteriores.
2. Tema novo: leia o capítulo e preencha as tabelas "X vs Y" de memória.
3. Prática: 10 questões de cenário do tema, respondidas antes de ler o gabarito.
4. Registro: anote cada erro em [Diário de erros](diario-de-erros.md).

## Cronograma

O prazo ainda depende da sua data-alvo e das horas semanais disponíveis.
Como referência, para quem já tem boa base de arquitetura:

| Semanas | Foco |
|---|---|
| 1 a 2 | Fase 1 e segurança (S3) |
| 3 a 4 | Resiliência (S1, S2, S4) |
| 5 a 6 | Performance, armazenamento, analytics (S5, S7) e rede híbrida (S6) |
| 7 | Custo e migração (S8) |
| 8 | Simulados completos e revisão dos erros |

Ajuste conforme o seu ritmo: o que importa é o critério de prontidão, não o
calendário.

---
**Próximo:** [Mapa de cobertura →](mapa-de-cobertura.md)
