[⬅ Sumário](../../README.md) · Trilha SAA-C03 · S6 de 8

# S6. Conectividade híbrida e rede avançada

Este capítulo estende o [Capítulo 3 (VPC)](../03-redes-vpc.md): como ligar VPCs entre si, ligar o
on-premises à AWS e acessar serviços de forma privada.

## Conectar VPCs

| Opção | O que faz | Limites |
|---|---|---|
| **VPC Peering** | conexão privada direta entre duas VPCs (mesma ou outra região/conta) | não é transitiva; CIDRs não podem se sobrepor |
| **Transit Gateway (TGW)** | hub central que conecta muitas VPCs e redes on-premises (modelo hub-and-spoke) | cobrado por anexo e por GB; suporta roteamento transitivo |
| **PrivateLink** | expõe um serviço de uma VPC a outras, de forma privada, via interface endpoint | unidirecional (consumidor para provedor); sem problema de CIDR sobreposto |

Como decidir:

- Duas ou três VPCs, simples → **peering**.
- Dezenas de VPCs e conexão com on-premises → **Transit Gateway**.
- Expor **um serviço específico** (sem abrir a rede inteira) ou conectar
  VPCs com CIDR sobreposto → **PrivateLink**.

## Conectar o on-premises à AWS

| Opção | Característica | Quando usar |
|---|---|---|
| **Site-to-Site VPN** | túnel IPsec criptografado pela internet; pronto em minutos; até ~1,25 Gbps por túnel | rápido, barato, backup do Direct Connect |
| **Direct Connect (DX)** | link dedicado e privado; latência e banda consistentes (1, 10 ou 100 Gbps); leva semanas para provisionar | tráfego alto e constante, latência previsível |
| **DX + VPN** | VPN sobre o Direct Connect | criptografia sobre o link dedicado |
| **Client VPN** | acesso remoto de usuários individuais à VPC | colaboradores remotos |

Detalhes de prova:

- Direct Connect **não é criptografado por padrão**. Para criptografar, use
  VPN sobre DX (IPsec) ou MACsec nas conexões dedicadas compatíveis.
- **Direct Connect Gateway** liga um DX a VPCs de várias regiões.
- **Alta disponibilidade do DX**: duas conexões em locais distintos; ou DX
  principal com VPN de backup.
- Precisa estar no ar já, sem esperar semanas → **VPN** primeiro.

## Acesso privado a serviços AWS

- **Gateway endpoint** (S3, DynamoDB): gratuito, entra na route table.
- **Interface endpoint (PrivateLink)**: ENI com IP privado na sua subnet,
  cobrado por hora e por GB.
- Acesso ao S3 a partir do on-premises, pelo Direct Connect/VPN, sem passar
  pela internet: use um **interface endpoint** para o S3 (o gateway endpoint
  não é alcançável de fora da VPC).

## Saída para a internet

| Recurso | Função |
|---|---|
| **Internet Gateway** | entrada e saída bidirecional para subnets públicas |
| **NAT Gateway** | saída para a internet a partir de subnets privadas (sem entrada); por AZ, para resiliência |
| **Egress-only Internet Gateway** | saída apenas, para IPv6 |
| **Elastic IP** | IP público estático |

Para resiliência, use um NAT Gateway **por AZ**. Um único NAT é ponto único de
falha para a AZ onde está.

## Observabilidade de rede

- **VPC Flow Logs**: metadados de tráfego IP (aceito/rejeitado) para
  diagnóstico e auditoria.
- **Traffic Mirroring**: copia o tráfego de interfaces de rede para inspeção.
- **Reachability Analyzer**: valida se há caminho entre dois recursos e aponta
  onde bloqueia.

## IPv6 e DNS

- A VPC pode ter blocos IPv4 e IPv6 (dual-stack).
- **Route 53 Resolver**: resolve nomes entre VPC e on-premises com endpoints
  de entrada e saída.

## Para o exame

- Peering **não é transitivo**: A↔B e B↔C não implica A↔C.
- Muitas VPCs e on-premises → **Transit Gateway**.
- Alto volume, estável, latência previsível → **Direct Connect**.
- Rápido e barato, ou backup do DX → **VPN**.
- Expor serviço sem abrir a rede, ou CIDR sobreposto → **PrivateLink**.
- NAT Gateway é recurso **zonal**; para HA, um por AZ.

---
**Anterior:** [← S5. EBS, EFS e FSx](05-armazenamento-ebs-efs-fsx.md) | **Próximo:** [S7. Analytics, integração e containers →](07-analytics-integracao-containers.md)
