[⬅ Sumário](../../README.md) · Trilha SAA-C03 · S5 de 8

# S5. Armazenamento em bloco, arquivo e híbrido: EBS, EFS, FSx e Storage Gateway

Domínios: performance e custo. O [Capítulo 5](../05-armazenamento-s3.md) trata do
S3 (objetos). Aqui estão os outros tipos de armazenamento que a prova cobra.

## Os três modelos de armazenamento

| | Bloco (EBS) | Arquivo (EFS, FSx) | Objeto (S3) |
|---|---|---|---|
| Acesso | volume anexado a uma instância | sistema de arquivos compartilhado (NFS/SMB) | API HTTP |
| Compartilhado entre instâncias | geralmente não (Multi-Attach é exceção) | sim | sim |
| Uso típico | disco de SO, banco de dados | conteúdo compartilhado, home dirs | backups, data lake, estáticos |

## Amazon EBS

Volume de bloco persistente, **preso a uma AZ**. Só anexa a instâncias da
mesma AZ.

| Tipo | Perfil | Quando usar |
|---|---|---|
| **gp3** | SSD uso geral; IOPS e throughput configuráveis independentes do tamanho | padrão para a maioria; mais barato que gp2 |
| gp2 | SSD uso geral; IOPS atrelado ao tamanho | legado |
| **io2 / io2 Block Express** | SSD de IOPS provisionado, alta durabilidade | bancos críticos que exigem IOPS sustentado e baixa latência |
| io1 | IOPS provisionado | legado |
| **st1** | HDD otimizado para throughput | big data, logs, leitura sequencial |
| **sc1** | HDD frio, o mais barato | dados raramente acessados, sequenciais |

Regras da prova:

- HDD (st1, sc1) **não pode ser volume de boot**.
- **Snapshots**: incrementais, armazenados no S3 (gerenciado pela AWS). Servem
  como backup e para mover um volume de AZ ou de região.
- **Mover um volume para outra AZ**: crie um snapshot e restaure na AZ de destino.
- **Criptografia**: com KMS, ativada na criação; snapshot de volume
  criptografado é criptografado.
- **Multi-Attach** (io1/io2): anexa o mesmo volume a várias instâncias na mesma
  AZ, para aplicações que gerenciam a escrita concorrente.
- **Instance store**: disco físico do host, de altíssima performance, porém
  **efêmero** (perde dados ao parar/terminar a instância). Use para cache e
  dados temporários.

## Amazon EFS

Sistema de arquivos **NFS gerenciado**, elástico e **multi-AZ**. Compatível com
Linux (não com Windows).

- Escala automaticamente; paga pelo que usa.
- Montado por centenas de instâncias de várias AZs ao mesmo tempo.
- **Classes**: Standard, Standard-IA, One Zone e One Zone-IA, com
  **lifecycle management** movendo arquivos pouco acessados para classes mais baratas.
- **Modos de performance**: General Purpose (padrão) e Max I/O. Modos de
  throughput: Elastic, Provisioned e Bursting.
- Acesso por **mount targets** dentro da VPC, protegidos por Security Group.

Quando usar: conteúdo web compartilhado, gerenciadores de conteúdo,
diretórios home, workloads Linux que precisam de arquivo compartilhado.

## Amazon FSx

Sistemas de arquivos gerenciados de terceiros e de alta performance.

| Variante | Para quê |
|---|---|
| **FSx for Windows File Server** | SMB, integra com Active Directory, NTFS; cargas Windows |
| **FSx for Lustre** | HPC, machine learning, processamento rápido de grandes volumes; integra com S3 |
| **FSx for NetApp ONTAP** | NFS, SMB e iSCSI; migração de ambientes NetApp, recursos de dados avançados |
| **FSx for OpenZFS** | migração de ZFS/NFS Linux com baixa latência |

Dica: "Windows + SMB + AD" → FSx for Windows. "HPC / ML com milhões de IOPS" →
FSx for Lustre. "Linux compartilhado simples e elástico" → EFS.

## Storage Gateway e transferência híbrida

| Gateway | Apresenta | Usa |
|---|---|---|
| **S3 File Gateway** | NFS/SMB | objetos no S3, com cache local |
| **FSx File Gateway** | SMB | acesso local de baixa latência ao FSx for Windows |
| **Volume Gateway** (cached ou stored) | iSCSI | volumes com snapshots no S3/EBS |
| **Tape Gateway** | biblioteca de fitas virtuais (VTL) | substitui fitas físicas, arquivando no S3/Glacier |

Use Storage Gateway quando o dado continua sendo acessado do on-premises, mas
a retenção e o backup devem ir para a nuvem sem alterar a aplicação. Para
mover grandes volumes de uma vez, veja [S8](08-migracao-transferencia-e-custos.md).

## Escolha rápida

| Necessidade | Resposta |
|---|---|
| Disco de uma instância, uso geral | EBS gp3 |
| Banco crítico com IOPS altíssimo | EBS io2 |
| Throughput sequencial barato (big data) | EBS st1 |
| Arquivo compartilhado Linux entre AZs | EFS |
| Arquivo compartilhado Windows com AD | FSx for Windows |
| HPC e ML | FSx for Lustre |
| Cache/dados temporários de altíssima velocidade | Instance store |
| Acesso on-premises a objetos S3 como arquivo | S3 File Gateway |
| Arquivar fitas na nuvem | Tape Gateway |

## Para o exame

- EBS é **por AZ**; EFS é **por região (multi-AZ)**; S3 é **regional**.
- Compartilhar arquivos entre instâncias em várias AZs → **EFS**, não EBS.
- Dados da instance store somem se a instância parar.
- Reduzir custo do EBS sem perder performance → migrar gp2 para gp3.

---
**Anterior:** [← S4. DR e backup](04-dr-e-backup.md) | **Próximo:** [S6. Conectividade híbrida →](06-conectividade-hibrida.md)
