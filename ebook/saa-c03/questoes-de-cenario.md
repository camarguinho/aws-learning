[⬅ Sumário](../../README.md) · Trilha SAA-C03 · Banco de questões

# Banco de questões de cenário

Questões originais, no estilo da prova (cenário, restrição, melhor solução). Elas
treinam o raciocínio de escolha. Não substituem simulados completos de
fornecedores de qualidade, que você deve fazer na Fase 5 do [plano](00-plano-de-estudos.md).

## Como usar

1. Leia o cenário e **sublinhe a restrição principal** (custo, disponibilidade,
   segurança, latência, esforço operacional).
2. Responda antes de abrir o gabarito.
3. Abra o gabarito e leia a explicação de **todas** as alternativas, não só da
   correta.
4. Registre cada erro no [Diário de erros](diario-de-erros.md).

Questões 1 a 9: segurança. 10 a 17: resiliência. 18 a 24: performance.
25 a 30: custo. 31 e 32: múltipla resposta (escolha duas).

---

## Segurança

**1.** Uma empresa usa AWS Organizations com dezenas de contas. A área de
conformidade exige que nenhuma conta da OU de desenvolvimento crie recursos
fora da região `sa-east-1`. Qual solução atende com o menor esforço de manutenção?

- A. Criar uma política IAM em cada usuário de cada conta.
- B. Aplicar uma SCP na OU de desenvolvimento negando ações fora da região.
- C. Criar uma regra do AWS Config e corrigir manualmente.
- D. Ler o CloudTrail todos os dias.

<details><summary>Gabarito</summary>

**B.** A SCP define o teto de permissões de todas as contas da OU, de forma
preventiva e centralizada. A: não escala e é fácil de burlar. C: o Config
detecta, não impede. D: é auditoria manual, reativa.
</details>

**2.** Uma aplicação em instâncias EC2 precisa ler objetos de um bucket S3. Qual
é a forma mais segura de conceder acesso?

- A. Gravar uma chave de acesso IAM no arquivo de configuração.
- B. Anexar uma role IAM à instância (instance profile) com permissão mínima.
- C. Tornar o bucket público.
- D. Compartilhar as credenciais de um usuário administrador.

<details><summary>Gabarito</summary>

**B.** A role fornece credenciais temporárias, rotacionadas automaticamente, sem
segredo no código. A e D: credenciais de longa duração. C: expõe os dados a todos.
</details>

**3.** Uma empresa precisa descobrir automaticamente quais buckets S3 contêm dados
pessoais (PII), como CPF e e-mail. Qual serviço usar?

- A. Amazon GuardDuty
- B. Amazon Macie
- C. AWS Inspector
- D. AWS Shield

<details><summary>Gabarito</summary>

**B.** O Macie classifica dados sensíveis no S3. A: detecta ameaças e atividade
anômala. C: varre vulnerabilidades de software. D: protege contra DDoS.
</details>

**4.** Um site atrás de um Application Load Balancer sofre tentativas de SQL
injection. Qual solução protege a aplicação?

- A. Security Group bloqueando a porta 3306.
- B. AWS WAF associado ao ALB com regras gerenciadas contra SQL injection.
- C. AWS Shield Standard.
- D. Network ACL bloqueando o IP do atacante.

<details><summary>Gabarito</summary>

**B.** O WAF inspeciona a camada 7 e tem regras para SQL injection. A: a porta
do banco não é a do ataque HTTP. C: Shield cobre DDoS de camadas 3 e 4. D: os IPs
mudam, e o NACL não inspeciona o conteúdo da requisição.
</details>

**5.** Uma norma exige que a chave de criptografia dos dados no S3 seja controlada
pela empresa, com rotação anual automática e registro de todos os usos. O que
configurar?

- A. SSE-S3.
- B. SSE-KMS com uma chave gerenciada pelo cliente e rotação habilitada.
- C. Criptografia somente no cliente, sem KMS.
- D. Nenhuma, pois o S3 já criptografa por padrão.

<details><summary>Gabarito</summary>

**B.** A chave gerenciada pelo cliente dá controle de política e rotação, e os usos
ficam no CloudTrail. A e D: a chave é da AWS, sem controle nem auditoria por
uso. C: não atende ao requisito de rotação gerenciada e auditoria sem esforço.
</details>

**6.** Os arquivos estáticos estão em um bucket S3 privado, servidos por CloudFront.
Como impedir que usuários acessem o bucket diretamente, sem passar pelo CDN?

- A. Deixar o bucket público e confiar na URL longa.
- B. Usar Origin Access Control (OAC) e uma bucket policy que permita só o CloudFront.
- C. Usar um Security Group no bucket.
- D. Desligar o CloudFront.

<details><summary>Gabarito</summary>

**B.** O OAC permite que somente a distribuição acesse o bucket, que permanece
privado. A: segurança por obscuridade. C: S3 não usa Security Group. D: remove o
requisito de CDN.
</details>

**7.** Usuários de um aplicativo móvel fazem login e precisam enviar fotos
diretamente ao S3 com credenciais temporárias. Qual combinação usar?

- A. Criar um usuário IAM para cada pessoa.
- B. Amazon Cognito (user pool para login e identity pool para credenciais).
- C. Embutir uma chave de acesso no aplicativo.
- D. Tornar o bucket público para escrita.

<details><summary>Gabarito</summary>

**B.** O user pool autentica e o identity pool troca a identidade por credenciais
AWS temporárias com permissões limitadas. A: não escala. C: qualquer pessoa
extrai a chave. D: abre o bucket ao abuso.
</details>

**8.** Uma instância RDS foi criada sem criptografia e agora precisa ser
criptografada. Qual é o procedimento?

- A. Ativar a criptografia nas configurações da instância existente.
- B. Criar um snapshot, copiá-lo com criptografia KMS e restaurar uma nova instância.
- C. Criptografar o volume EBS subjacente diretamente.
- D. Não é possível.

<details><summary>Gabarito</summary>

**B.** A criptografia do RDS só é definida na criação; o caminho é snapshot,
cópia criptografada e restauração. A e C: não são possíveis em uma instância já
existente. D: existe o caminho via snapshot.
</details>

**9.** A empresa quer ser alertada quando uma instância EC2 passar a se comunicar com
um endereço IP malicioso conhecido ou tiver comportamento anômalo. Qual serviço?

- A. Amazon GuardDuty
- B. AWS Config
- C. Amazon Macie
- D. AWS Trusted Advisor

<details><summary>Gabarito</summary>

**A.** O GuardDuty analisa CloudTrail, VPC Flow Logs e logs de DNS para detectar
ameaças. B: avalia conformidade de configuração. C: dados sensíveis no S3. D:
recomendações gerais de boas práticas.
</details>

---

## Resiliência

**10.** Uma aplicação web em três camadas deve continuar disponível se uma
Zona de Disponibilidade inteira falhar. Qual arquitetura atende?

- A. Uma instância EC2 grande e um RDS Single-AZ.
- B. ALB, Auto Scaling Group em duas ou mais AZs e RDS Multi-AZ.
- C. Duas instâncias na mesma AZ.
- D. Backups diários do RDS.

<details><summary>Gabarito</summary>

**B.** Cada camada passa a existir em mais de uma AZ, com failover. A e C: ponto
único de falha na AZ. D: backup ajuda na recuperação, mas não mantém a
disponibilidade durante a falha.
</details>

**11.** Uma aplicação recebe picos que sobrecarregam o serviço de processamento
de pedidos, e pedidos são perdidos. Como tornar o sistema resiliente?

- A. Aumentar a instância do backend ao máximo.
- B. Colocar uma fila SQS entre a entrada de pedidos e os workers.
- C. Aumentar o timeout do cliente.
- D. Desligar a validação.

<details><summary>Gabarito</summary>

**B.** A fila absorve o pico e os workers processam no ritmo possível, sem perder
mensagens. A: tem teto e continua com ponto único de falha. C: não resolve a
perda. D: piora a qualidade dos dados.
</details>

**12.** Um sistema crítico exige RTO de 15 minutos e RPO de poucos segundos, e a
empresa quer pagar menos do que uma arquitetura ativa/ativa. Qual estratégia de
DR escolher?

- A. Backup e restauração.
- B. Pilot light sem o banco replicado.
- C. Warm standby com replicação contínua do banco para outra região.
- D. Nenhuma, só Multi-AZ.

<details><summary>Gabarito</summary>

**C.** O warm standby mantém uma versão reduzida e funcional já rodando, com banco
replicado, e escala na falha. A: horas de RTO/RPO. B: sem o banco replicado o RPO
é alto. D: Multi-AZ não cobre a falha de região.
</details>

**13.** Como apontar o domínio raiz (`exemplo.com`) para um Application Load
Balancer no Route 53?

- A. Registro CNAME.
- B. Registro Alias.
- C. Registro MX.
- D. Registro TXT.

<details><summary>Gabarito</summary>

**B.** O CNAME não é permitido na raiz do domínio; o Alias funciona nela e acompanha
os IPs do ALB. C e D: não resolvem nome para recurso.
</details>

**14.** A aplicação roda na região primária, com uma cópia na região secundária.
Se a primária falhar, o tráfego deve ser desviado automaticamente. O que usar?

- A. Route 53 com política de failover e health checks.
- B. Route 53 com política simples.
- C. Alterar o DNS manualmente na falha.
- D. Aumentar o TTL para 24 horas.

<details><summary>Gabarito</summary>

**A.** O health check detecta a falha e o Route 53 passa a responder com o
secundário. B: não monitora saúde. C: é manual e lento. D: TTL alto atrasa o
failover.
</details>

**15.** Um banco relacional precisa de réplicas de leitura em outra região, com
baixa latência de replicação e recuperação de desastre com RPO de segundos. Qual
opção escolher?

- A. RDS Single-AZ com snapshots diários.
- B. Amazon Aurora Global Database.
- C. Exportar dados para o S3 toda noite.
- D. RDS Multi-AZ na mesma região.

<details><summary>Gabarito</summary>

**B.** O Aurora Global Database replica entre regiões com latência de segundos.
A e C: RPO de horas. D: protege contra falha de AZ, não de região.
</details>

**16.** Para se proteger contra ransomware, a empresa quer que os backups não possam
ser apagados nem por administradores, e que existam cópias em outra conta. O que
configurar?

- A. Backups no mesmo volume da aplicação.
- B. AWS Backup com Vault Lock e cópia para um vault em outra conta.
- C. Snapshots manuais ocasionais.
- D. Aumentar o tamanho do disco.

<details><summary>Gabarito</summary>

**B.** O Vault Lock torna os backups imutáveis (WORM), e a cópia entre contas
protege contra o comprometimento de uma conta. A: o backup cai junto com o dado.
C: sem política nem imutabilidade. D: não tem relação.
</details>

**17.** Algumas mensagens de uma fila SQS falham repetidamente no processamento e
travam o fluxo. Como isolá-las para análise sem perder as demais?

- A. Apagar as mensagens com falha.
- B. Configurar uma Dead-Letter Queue com limite de recebimentos.
- C. Aumentar o tempo de retenção da fila principal.
- D. Reiniciar os consumidores a cada falha.

<details><summary>Gabarito</summary>

**B.** Após o número máximo de tentativas, a mensagem vai para a DLQ, onde pode
ser analisada. A: perde dados. C: não isola a mensagem. D: não resolve a causa.
</details>

---

## Performance

**18.** Uma empresa serve imagens e arquivos estáticos guardados no S3 para
usuários no mundo todo e quer reduzir a latência. Qual serviço usar?

- A. Amazon CloudFront.
- B. Um ALB na frente do bucket.
- C. Mais buckets na mesma região.
- D. Uma instância EC2 por país.

<details><summary>Gabarito</summary>

**A.** O CloudFront faz cache nos pontos de presença próximos ao usuário. B: o ALB
não é CDN. C: não reduz a distância. D: é caro e complexo.
</details>

**19.** Um jogo online usa tráfego UDP e precisa de IPs estáticos globais e
failover rápido entre regiões. Qual serviço atende?

- A. Amazon CloudFront.
- B. AWS Global Accelerator com NLB.
- C. Application Load Balancer.
- D. Route 53 com política simples.

<details><summary>Gabarito</summary>

**B.** O Global Accelerator oferece 2 IPs anycast, suporta TCP/UDP e faz failover
rápido sem depender de cache DNS. A: HTTP/HTTPS. C: não suporta UDP e não dá IP
estático. D: o cache de DNS atrasa a troca.
</details>

**20.** Várias instâncias Linux em diferentes AZs precisam compartilhar o mesmo
sistema de arquivos. Qual solução usar?

- A. Volume EBS gp3 anexado a todas.
- B. Amazon EFS.
- C. Instance store.
- D. Um volume EBS por instância, sincronizado manualmente.

<details><summary>Gabarito</summary>

**B.** O EFS é NFS gerenciado e multi-AZ. A: o EBS é preso a uma AZ e, em geral, a
uma instância. C: é efêmero e local. D: é frágil e trabalhoso.
</details>

**21.** O banco RDS sofre com milhares de consultas de leitura idênticas e
repetidas, sobrecarregando a instância. Qual medida reduz a carga com a menor
mudança de arquitetura?

- A. Trocar para uma instância maior, apenas.
- B. Adicionar uma camada de cache com Amazon ElastiCache para os resultados.
- C. Desligar os índices.
- D. Mover tudo para o S3.

<details><summary>Gabarito</summary>

**B.** O cache atende leituras repetidas sem tocar no banco, com latência de
milissegundos. A: custa mais e não ataca a repetição. C: piora as consultas. D:
muda o modelo de acesso e não substitui um banco transacional.
</details>

**22.** Uma equipe quer executar consultas SQL ad hoc em arquivos de log armazenados
no S3, sem provisionar servidores. Qual serviço usar?

- A. Amazon Redshift com cluster sempre ligado.
- B. Amazon Athena.
- C. Amazon EMR.
- D. RDS com carga manual.

<details><summary>Gabarito</summary>

**B.** O Athena executa SQL diretamente sobre o S3, sem servidores, pagando pelo
dado varrido. A e C: exigem cluster. D: exige carga e administração.
</details>

**23.** Dados de cliques chegam em tempo real e vários sistemas independentes
precisam ler o mesmo fluxo e poder reprocessá-lo. O que usar?

- A. Amazon SQS standard.
- B. Amazon Kinesis Data Streams.
- C. E-mail via SES.
- D. Um arquivo CSV no S3 atualizado a cada hora.

<details><summary>Gabarito</summary>

**B.** O Kinesis Data Streams permite vários consumidores lendo o mesmo stream e
reprocessamento dentro da retenção. A: cada mensagem é consumida e removida.
C e D: não atendem ao tempo real e ao reprocessamento.
</details>

**24.** Um banco de dados crítico em EC2 exige IOPS muito altos, sustentados e com
baixa latência. Qual tipo de volume EBS é o mais indicado?

- A. st1.
- B. sc1.
- C. io2.
- D. gp2 com volume pequeno.

<details><summary>Gabarito</summary>

**C.** O io2 é SSD de IOPS provisionado, para cargas críticas. A e B: HDD
voltado a throughput sequencial. D: o IOPS do gp2 depende do tamanho.
</details>

---

## Custo

**25.** Um processamento em lote noturno é tolerante a interrupções e pode ser
reiniciado. Qual modelo de compra minimiza o custo?

- A. On-Demand.
- B. Reserved Instances de 3 anos.
- C. Spot Instances.
- D. Dedicated Host.

<details><summary>Gabarito</summary>

**C.** O Spot oferece até cerca de 90% de desconto para cargas tolerantes a
interrupção. A: preço cheio. B: compromisso longo para uma carga noturna. D: o
mais caro, sem necessidade.
</details>

**26.** Uma carga de produção estável deve rodar pelos próximos 3 anos, e a empresa
quer a maior flexibilidade para trocar de família de instância e de região. O que
comprar?

- A. Reserved Instances conversíveis de uma só família.
- B. Compute Savings Plans de 3 anos.
- C. Somente On-Demand.
- D. Spot para tudo.

<details><summary>Gabarito</summary>

**B.** O Compute Savings Plan dá desconto grande e se aplica a várias famílias,
regiões e até a Fargate e Lambda. A: menos flexível. C: mais caro. D: não é
adequado a uma carga de produção estável.
</details>

**27.** Uma aplicação em subnets privadas acessa muito o S3 pelo NAT Gateway, gerando
uma conta alta. Como reduzir o custo mantendo o tráfego privado?

- A. Adicionar mais NAT Gateways.
- B. Criar um Gateway VPC Endpoint para o S3.
- C. Passar o tráfego pela internet pública.
- D. Trocar o S3 por EBS.

<details><summary>Gabarito</summary>

**B.** O Gateway Endpoint do S3 é gratuito e tira o tráfego do NAT. A: aumenta o
custo. C: piora segurança e continua cobrando saída. D: não equivale
funcionalmente.
</details>

**28.** Os objetos de um bucket têm padrão de acesso imprevisível, e a empresa quer
reduzir o custo sem perder a recuperação imediata. Qual classe escolher?

- A. S3 Glacier Deep Archive.
- B. S3 Intelligent-Tiering.
- C. S3 One Zone-IA para tudo.
- D. S3 Standard para tudo.

<details><summary>Gabarito</summary>

**B.** O Intelligent-Tiering move objetos entre camadas conforme o acesso, sem
penalidade de recuperação. A: recuperação demorada. C: sem redundância multi-AZ.
D: não otimiza o custo.
</details>

**29.** Uma empresa precisa enviar 80 TB para a AWS, tem apenas um link de 100 Mbps
e o prazo é de poucas semanas. Qual abordagem usar?

- A. Enviar tudo pela internet.
- B. Usar um dispositivo da família Snow (Snowball Edge).
- C. Pedir ao pessoal para enviar por e-mail.
- D. Comprimir e esperar.

<details><summary>Gabarito</summary>

**B.** 80 TB a 100 Mbps levam cerca de 2 a 3 meses, então o transporte físico offline
é a resposta. A, C e D: não cumprem o prazo. Confirme a disponibilidade atual dos
dispositivos Snow, como indicado no [S8](08-migracao-transferencia-e-custos.md).
</details>

**30.** Uma licença de software é cobrada por soquete físico do servidor, e o
fornecedor exige comprovar o hardware dedicado. Qual opção de EC2 usar?

- A. Instâncias Spot.
- B. Dedicated Host.
- C. Instâncias On-Demand compartilhadas.
- D. Lambda.

<details><summary>Gabarito</summary>

**B.** O Dedicated Host dá visibilidade de soquetes e núcleos, atendendo
licenças por hardware físico. A e C: hardware compartilhado. D: não se aplica.
</details>

---

## Múltipla resposta (escolha duas)

**31.** Uma aplicação web roda em uma única instância EC2, em uma só AZ, e
indisponibilidades são frequentes. Quais DUAS ações aumentam a disponibilidade?

- A. Colocar as instâncias em um Auto Scaling Group com duas ou mais AZs.
- B. Aumentar o tipo da instância.
- C. Colocar um Application Load Balancer na frente das instâncias.
- D. Associar um Elastic IP.
- E. Aumentar o tamanho do volume EBS.

<details><summary>Gabarito</summary>

**A e C.** O ASG multi-AZ repõe e distribui as instâncias, e o ALB direciona o
tráfego só para as saudáveis. B e E: continuam sendo ponto único de falha. D: IP
fixo não dá redundância.
</details>

**32.** Uma função Lambda acessa uma tabela DynamoDB e um bucket S3. Quais DUAS
práticas seguem o princípio do menor privilégio?

- A. Usar a role de execução da Lambda, com permissões restritas.
- B. Gravar uma chave de acesso IAM no código da função.
- C. Usar `Action: *` e `Resource: *` para evitar erros de permissão.
- D. Limitar as permissões às ações necessárias e aos ARNs específicos da tabela e do bucket.
- E. Usar o usuário administrador da conta.

<details><summary>Gabarito</summary>

**A e D.** A role dá credenciais temporárias, e a política restrita a ações e
recursos específicos limita o dano de uma falha. B e E: credenciais de longa
duração e amplas. C: acesso total, o oposto do menor privilégio.
</details>

---

## Placar

| Data | Questões feitas | Acertos | % | Domínio mais fraco |
|---|---|---|---|---|
| | | | | |

---
**Anterior:** [← S8. Migração, transferência e custos](08-migracao-transferencia-e-custos.md) | **Próximo:** [Laboratórios →](labs.md)
