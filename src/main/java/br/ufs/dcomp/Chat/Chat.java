package br.ufs.dcomp.Chat;

import com.rabbitmq.client.*;

import java.io.IOException;

import java.util.Scanner;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Chat {

  private final static String QUEUE_NAME = "minha-fila";
  private final static DateFormat sdf = new SimpleDateFormat("'('dd/MM/yyyy 'às' HH:mm:ss') '");
  //public static boolean novo_login = true;
  public static boolean novo_destino;
  public static String Destino = new String("");

  public static void main(String[] argv) throws Exception {
    Scanner entrada = new Scanner(System.in);
    
    ConnectionFactory factory = new ConnectionFactory();
    //factory.setUri("amqp://huarumck:VncxT9rNIpuDuLcCkfJqne0JWAlKbA0k@otter.rmq.cloudamqp.com/huarumck");
    factory.setHost("ec2-34-220-41-87.us-west-2.compute.amazonaws.com");
    factory.setUsername("accelwo");
    factory.setPassword("meupass");
    factory.setVirtualHost("/");
    
    //  Cria a conexão e o canal
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    
    //  Inicialização do programa
    System.out.print("Usuário: ");
    String user = entrada.nextLine();
    System.out.println("Bem vindo " + user);
    
    
    channel.queueDeclare(user, false, false, false, null);
    //System.out.println(" [*] Carregando Mensagens salvas!  [*]");

    Consumer consumer = new DefaultConsumer(channel) {
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
        String message = new String(body, "UTF-8");
        System.out.println("");
        System.out.println(message);
        System.out.print(Destino + ">> ");
      }
    };
    channel.basicConsume(user, true, consumer);
    
    //  O programa entra em loop infitnito pra o envio de mensagens
    //  para fechar o programa basta digitar #fechar
    while (true){
      //  Aguarda e recebe a mensagem a ser enviada
      System.out.print(Destino + ">> ");
      String message = entrada.nextLine();
      
      //  Verifica se a mensagem está vazia, caso esteja a subistitui por um " "
      if (message.length() == 0){
        message = " ";
      }
      
      //  Verifica o primeiro character da menssagem
      if (message.substring(0,1).equals("@")){
        //  Caso seja @ ele separa o texto e marca ele como Destinatário das mensagens
        Destino = message.substring(1,message.length());
        novo_destino = true;
      }
      
      
      //Registra a Data e Hora atual
      Date data = new Date();
  
      //  Adiciona informações de data e usuário á mensagem a ser enviada
      String texto = sdf.format(data) + user + " diz: " + message; 
      
      //  Caso o usuário digite #fechar, finaliza o programa
      if (message.equals("!fechar")){
        System.out.println("[*]  ==== Programa Finalizado =======    [*]");
        System.exit(0);
      }
      
      //  Verifica se o texto digitado no terminal é pra selecionar um novo destino
      if (novo_destino){
        //  Se for um novo destinatário, ele não irá enviar mensagem pra ninguem
        novo_destino = false;
        
      } else {
        //  Verifica se o Destinatário passado é vazio
        if (!Destino.equals("")){
          //  Se não for vazio, envia a mensagem
          channel.basicPublish("", Destino, null, texto.getBytes("UTF-8"));  
          
        } else {
          // Se o destinatário for válido, envia a mensagem
          System.out.println("[*] Erro: Nenhum destinatario selecionado  [*]");
          System.out.println("[*] Por favor utilize o comando @nome para [*]");
          System.out.println("[*] selecionar o destinatario              [*]");
        }
      }
    }
  }
}