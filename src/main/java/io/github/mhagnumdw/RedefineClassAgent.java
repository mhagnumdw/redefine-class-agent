package io.github.mhagnumdw;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.sun.tools.attach.VirtualMachine;

public class RedefineClassAgent {

    private static final String AGENT_ARGS_SEPARATOR = ",";

    // Referência ao objeto Instrumentation obtido via agentmain/premain.
    private static Instrumentation instrumentation;

    /**
     * Método chamado quando o agente é anexado dinamicamente a uma JVM em execução.
     *
     * @param agentArgs parâmetros do agente
     * @param inst instrumentation
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        logI("Agent anexado dinamicamente");
        agentExecute(agentArgs, inst);
    }

    /**
     * Método chamado se o agente for carregado na inicialização da JVM (usando -javaagent).
     *
     * @param agentArgs parâmetros do agente
     * @param inst instrumentation
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        logI("Agent carregado no startup");
        agentExecute(agentArgs, inst);
    }

    private static void agentExecute(String agentArgs, Instrumentation inst) {
        instrumentation = inst;
        String[] parts = agentArgs.split(AGENT_ARGS_SEPARATOR, 2);
        String pathToNewClass = parts[0].trim();
        String fqdn = parts[1].trim();
        updateClass(fqdn, pathToNewClass, inst);
    }

    /**
     * Realiza a redefinição da classe usando o bytecode lido do arquivo especificado.
     *
     * @param fqdn fully qualified domain name da classe
     * @param pathToNewClass path no disco para a nova classe (arquivo .class)
     * @param inst instrumentation
     */
    private static void updateClass(String fqdn, String pathToNewClass, Instrumentation inst) {
        try {
            // Lê o novo bytecode do arquivo
            byte[] newBytecode = Files.readAllBytes(Paths.get(pathToNewClass));

            // Obtém todas as classes carregadas
            Class<?> targetClass = null;
            for (Class<?> clazz : inst.getAllLoadedClasses()) {
                if (clazz.getName().equals(fqdn)) {
                    targetClass = clazz;
                    logI("Classe encontrada na JVM: " + fqdn);
                    break;
                }
            }

            if (targetClass == null) {
                logE("Classe não encontrada na JVM: " + fqdn);
                return;
            }

            ClassDefinition def = new ClassDefinition(targetClass, newBytecode);

            instrumentation.redefineClasses(def);
            logI("Classe atualizada com sucesso: " + fqdn);
        } catch (Exception e) {
            logE("Falha ao atualizar classe na JVM", e);
        }
    }

    /**
     * Método main para anexar este agente a uma JVM em execução.
     */
    public static void main(String[] args) {
        if (args.length == 1 && args[0].toLowerCase().contains("help")) {
            usage("Help");
            System.exit(1);
        }
        if (args.length != 2) {
            usage("Deve ser informado o PID da JVM e os parâmetros do agente, conforme exemplo:");
            System.exit(1);
        }
        String pid = args[0];
        String agentArgs = args[1];

        try {
            // Obtém o caminho para o JAR do agente
            String agentJar = RedefineClassAgent.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            logI("[Main] JVM PID   = " + pid);
            logI("[Main] agentArgs = " + agentArgs);
            logI("[Main] agentJar  = " + agentJar);

            if (agentArgs.split(AGENT_ARGS_SEPARATOR, -1).length != 2) {
                usage("Os parâmetros do agente devem ser conforme o exemplo:");
                System.exit(1);
            }

            // Anexa à JVM de destino cujo PID foi passado como argumento
            VirtualMachine vm = VirtualMachine.attach(pid);

            // Carrega o agente na JVM de destino
            vm.loadAgent(agentJar, agentArgs);
            vm.detach();
            logI("[Main] Agente carregado na JVM de destino com sucesso");
        } catch(Exception e) {
            logE("[Main] Falha ao carregar agente na JVM", e);
            System.exit(1);
        }
    }

    private static void usage(String msg) {
        System.out.println(msg);
        System.out.println(
            "Uso: java -cp \"target/RedefineClassAgent.jar:$JDK_HOME/lib/tools.jar\" io.github.mhagnumdw.RedefineClassAgent $JVM_PID $CLASS_FILE_PATH,$CLASS_FQDN");
        System.out.println(
            " Ex: java -cp \"target/RedefineClassAgent.jar:$JDK_HOME/lib/tools.jar\" io.github.mhagnumdw.RedefineClassAgent 123 /tmp/TesteMDB.class,br.com.mypackage.TesteMDB");
    }

    private static void logI(String msg) {
        System.out.println("[ INFO] [Agent] " + msg);
    }

    private static void logE(String msg) {
        System.err.println("[ERROR] [Agent] " + msg);
    }

    private static void logE(String msg, Exception ex) {
        System.err.println("[ERROR] [Agent] " + msg);
        ex.printStackTrace();
    }
}
