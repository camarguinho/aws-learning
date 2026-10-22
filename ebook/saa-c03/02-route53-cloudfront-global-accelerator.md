[⬅ Sumário](../../README.md) · Trilha SAA-C03 · S2 de 8

# S2. Route 53, CloudFront e Global Accelerator

Domínios: resiliência e performance. Estes três serviços resolvem "como o
usuário chega à aplicação, com baixa latência e sobrevivendo a falhas".

## Amazon Route 53

DNS gerenciado, com alta disponibilidade, registro de domínio e health checks.

### Tipos de registro que importam

| Registro | Uso |
|---|---|
| A / AAAA | nome para IPv4 / IPv6 |
| CNAME | nome para outro nome; não pode ser usado no apex (raiz) do domínio |
| **Alias** | específico da AWS: aponta para ALB, CloudFront, S3 (site), API Gateway etc.; funciona no apex; sem custo de consulta; acompanha mudanças de IP |
| MX, TXT | e-mail, verificações |

Regra de prova: para apontar a raiz do domínio (`exemplo.com`) para um ALB ou
CloudFront, a resposta é **Alias**, não CNAME.

### Políticas de roteamento

| Política | Comportamento | Quando usar |
|---|---|---|
| Simple | um registro, um ou mais valores | caso básico |
| Weighted | divide por pesos | canary, migração gradual, teste A/B |
| Latency | envia para a região de menor latência | usuários globais, várias regiões |
| Failover | primário/secundário com health check | DR ativo/passivo |
| Geolocation | por país/continente do usuário | conteúdo regional, conformidade legal |
| Geoproximity | por proximidade, com bias ajustável | deslocar tráfego entre regiões |
| Multivalue answer | vários IPs saudáveis | balanceamento simples por DNS |
| IP-based | por CIDR de origem | roteamento por provedor/rede |

Confusão clássica: **Latency** escolhe a menor latência medida; **Geolocation**
escolhe pela localização declarada do usuário, independentemente de
latência.

### Health checks

Verificam endpoints, outros health checks (calculated) ou alarmes
CloudWatch. São a base do failover. Para recursos privados, use um alarme
CloudWatch, pois os health checkers ficam fora da sua VPC.

## Amazon CloudFront

CDN com mais de 400 pontos de presença. Faz cache de conteúdo perto do
usuário.

- **Origens**: S3, ALB, EC2, API Gateway ou qualquer endpoint HTTP.
- **Origin Access Control (OAC)**: permite que só o CloudFront leia o bucket S3
  (o bucket fica privado). Substitui o antigo OAI.
- **Cache**: controlado por TTL, cache policy e headers. **Invalidation** remove
  objetos antes do TTL (tem custo após a cota gratuita).
- **HTTPS** com certificados do ACM (para CloudFront, o certificado precisa
  estar em **us-east-1**).
- **Signed URLs / signed cookies**: acesso a conteúdo privado por tempo
  limitado (streaming, downloads pagos).
- **Geo restriction**: bloqueia ou libera países.
- **Field-level encryption** protege campos sensíveis de ponta a ponta.
- **Lambda@Edge / CloudFront Functions**: lógica na borda (reescrita de URL,
  autenticação leve, headers).
- Integra com **WAF** e **Shield Standard** (gratuito, automático).

Serve para conteúdo estático e também dinâmico (reduz latência por usar a rede
backbone da AWS até a origem).

## AWS Global Accelerator

Dá **2 IPs anycast estáticos** que entram na rede global da AWS no ponto mais
próximo do usuário e seguem até a aplicação (ALB, NLB, EC2, Elastic IP).

| | CloudFront | Global Accelerator |
|---|---|---|
| Camada | HTTP/HTTPS | TCP e UDP |
| Faz cache | sim | não |
| IP estático | não | sim (2 IPs anycast) |
| Failover entre regiões | via origin groups | automático, rápido, por health check |
| Caso típico | sites, mídia, APIs com cache | jogos, VoIP, IoT, apps que precisam de IP fixo e failover rápido |

## Quando usar o quê

| Necessidade | Resposta |
|---|---|
| Reduzir latência de conteúdo estático/web global | CloudFront |
| Servir S3 privado só via CDN | CloudFront + OAC |
| Raiz do domínio apontando para ALB | Route 53 Alias |
| DR com failover automático por DNS | Route 53 Failover + health check |
| Distribuir usuários por região de menor latência | Route 53 Latency |
| IP fixo global, TCP/UDP, failover rápido | Global Accelerator |
| Liberar conteúdo apenas em certos países (legal) | CloudFront geo restriction ou Route 53 Geolocation |

## Para o exame

- O **cache DNS** (TTL) atrasa o failover do Route 53. Global Accelerator não
  sofre disso, pois usa IPs fixos.
- CloudFront não é só para estático: aceita métodos HTTP completos e origens
  customizadas.
- Certificado de CloudFront: **ACM em us-east-1**.
- Route 53 Alias para recursos AWS não gera custo de consulta; CNAME gera.

---
**Anterior:** [← S1. ELB e Auto Scaling](01-elb-e-auto-scaling.md) | **Próximo:** [S3. Segurança: KMS, WAF e governança →](03-seguranca-kms-waf-governanca.md)
