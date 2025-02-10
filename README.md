# RedefineClassAgent

O **RedefineClassAgent** é um agente Java que permite a redefinição de classes em uma JVM (Java Virtual Machine) em execução. É utilizada a capacidade de instrumentação da JVM para substituir o bytecode de classes já carregadas, permitindo atualizações dinâmicas sem a necessidade de reiniciar a aplicação.

## Versão do Java

O agente foi testado para Java 8, mas deve ser facilmente adaptado para outras versões. Veja o `pom.xml` e altere para a versão do Java desejada.

O agente depende do `tools.jar`, presente apenas na JDK (não é presente na JRE).

## Build do agente

Para empacotar o agente e gerar um arquivo JAR executável:

```bash
./mvnw -V clean package
```

**Observações:**

- O arquivo JAR será gerado em `target/RedefineClassAgent.jar`
- O parâmetro `-V` imprime a versão do Java, confira

## Como Executar

Para executar o agente e anexá-lo a uma JVM em execução, siga os passos abaixo.

### Obter o PID da JVM

```bash
ps aux | grep java
# ou
jps
# ou
jps -lvm
```

### Executar o agente

```bash
java \
  -cp "target/RedefineClassAgent.jar:$JDK_HOME/lib/tools.jar" \
  io.github.mhagnumdw.RedefineClassAgent \
  $JVM_PID \
  $CLASS_FILE_PATH,$CLASS_FQDN
```

**Substitua:**

- `$JDK_HOME` pelo path raiz da JDK
- `$JVM_PID` pelo ID do processo da JVM
- `$CLASS_FILE_PATH` pelo caminho completo para o arquivo `.class` que contém a nova definição da classe
- `CLASS_FQDN` pelo nome totalmente qualificado (FQDN) da classe que deseja redefinir, ou seja, no formato `my.package.Classe`

> Um exemplo completo:
>
> ```bash
> export JDK_HOME="/home/mhagnumdw/.asdf/installs/java/adoptopenjdk-8.0.362+9"
> export JVM_PID=$(jps | grep -P '^\d+ Main$' | awk '{print $1}')
> java -cp "target/RedefineClassAgent.jar:$JDK_HOME/lib/tools.jar" io.github.mhagnumdw.RedefineClassAgent $JVM_PID /tmp/TesteMDB.class,br.com.mypackage.TesteMDB
> ```
