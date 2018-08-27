package br.ufs.dcomp.Chat;

import com.rabbitmq.client.*;

import java.io.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.*;
import java.util.Scanner;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.ByteString;


public class Chat {

  private final static String QUEUE_NAME = "minha-fila";
  //private final static DateFormat sdf = new SimpleDateFormat("'('dd/MM/yyyy 'às' HH:mm:ss') '");
  private final static DateFormat t_data = new SimpleDateFormat("dd/MM/yyyy");
  private final static DateFormat t_hora = new SimpleDateFormat("HH:mm:ss");
  //public static boolean novo_login = true;
  public static boolean novo_destino;
  public static String Destino = new String("");
  public static boolean to_group = false;

  public static void main(String[] argv) throws Exception {
    Scanner entrada = new Scanner(System.in);
    
    ConnectionFactory factory = new ConnectionFactory();
    //factory.setUri("amqp://huarumck:VncxT9rNIpuDuLcCkfJqne0JWAlKbA0k@otter.rmq.cloudamqp.com/huarumck");
    factory.setHost("ec2-54-200-22-72.us-west-2.compute.amazonaws.com");
    factory.setUsername("accel");
    factory.setPassword("@Accel27");
    factory.setVirtualHost("/");
    
    //  Cria a conexão e o canal
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    Channel channel_f = connection.createChannel();
    
    //  Inicialização do programa
    System.out.print("Usuário: ");
    String user = entrada.nextLine();
    System.out.println("Bem vindo " + user);
    
    
    //Testando serializaçao
    
    //    COMENTARIO DE SERIALIZACAO
/*
  
    msgProto.Conteudo.builder cnt = msgProto.Conteudo.newBuilder();
    cnt.setTipoValue("text/plain");
    cnt.setCorpo(texto.getBytes("UTF-8"));
    cnt.setNome("teste");
    
    
    msgProto.Mensagem.builder mmsg = msgProto.Mensagem.newBuilder();
    mmsg.setEmissor("teste");
    mmsg.setData(data);
    mmsg.setHora(data);
    mmsg.setConteudo(cnt);*/
    
    
    
    channel.queueDeclare(user, false, false, false, null);
    channel_f.queueDeclare(user + "_f", false, false, false, null);
    //System.out.println(" [*] Carregando Mensagens salvas!  [*]");


    //Receptor de mensagens de texto
    Consumer consumer = new DefaultConsumer(channel) {
      /*public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
        String message = new String(body, "UTF-8");
        System.out.println("");
        System.out.println(message);
        System.out.print(Destino + ">> ");*/
        
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
            
          msgProto.Mensagem rec_mensagem =  msgProto.Mensagem.parseFrom(body);
          msgProto.Conteudo rec_conteudo = rec_mensagem.getConteudo();
          
          //System.out.println("RECEBIDA NA FILA Normal");
          String r_emissor = rec_mensagem.getEmissor();
          String r_data    = rec_mensagem.getData();
          String r_hora    = rec_mensagem.getHora();
          String r_grupo   = rec_mensagem.getGrupo();
          String r_tipo    = rec_conteudo.getTipo();
          String r_bytes   = rec_conteudo.getCorpo().toStringUtf8();
          String r_nome    = rec_conteudo.getNome();
          String msg_formatada = "(" + r_data + " às " + r_hora + ") " + r_emissor;
          
          if (!r_grupo.equals("")) {
            /*
            String msg_formatada = "(" + r_data + " às " + r_hora + ") " + r_emissor + " diz: " + r_bytes;
            System.out.println("");
            System.out.println(msg_formatada);
            System.out.print(Destino + ">> ");*/
            msg_formatada = msg_formatada + "#";/*
          } else {/*
            String msg_formatada = "(" + r_data + " às " + r_hora + ") " + r_emissor + "#" + r_grupo + " diz: " + r_bytes;
            System.out.println("");
            System.out.println(msg_formatada);
            System.out.print(Destino + ">> ");*/
          }
          msg_formatada = msg_formatada + r_grupo + " diz: " + r_bytes;
          System.out.println("");
          System.out.println(msg_formatada);
          System.out.print(Destino + ">> ");
          
        }
    };
    
     //Receptor de mensagens de arquivos _f
    Consumer consumer_f = new DefaultConsumer(channel_f) {   
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
            
          msgProto.Mensagem rec_mensagem =  msgProto.Mensagem.parseFrom(body);
          msgProto.Conteudo rec_conteudo = rec_mensagem.getConteudo();
          
          System.out.println("RECEBIDA NA FILA _F");
          String r_emissor = rec_mensagem.getEmissor();
          String r_data    = rec_mensagem.getData();
          String r_hora    = rec_mensagem.getHora();
          String r_grupo   = rec_mensagem.getGrupo();
          String r_tipo    = rec_conteudo.getTipo();
          String r_bytes   = rec_conteudo.getCorpo().toStringUtf8();
          String r_nome    = rec_conteudo.getNome();
          String msg_formatada = "(" + r_data + " às " + r_hora + ") " + r_emissor;
          
          if (!r_grupo.equals("")) {
            /*
            String msg_formatada = "(" + r_data + " às " + r_hora + ") " + r_emissor + " diz: " + r_bytes;
            System.out.println("");
            System.out.println(msg_formatada);
            System.out.print(Destino + ">> ");*/
            msg_formatada = msg_formatada + "#";/*
          } else {/*
            String msg_formatada = "(" + r_data + " às " + r_hora + ") " + r_emissor + "#" + r_grupo + " diz: " + r_bytes;
            System.out.println("");
            System.out.println(msg_formatada);
            System.out.print(Destino + ">> ");*/
          }
          msg_formatada = msg_formatada + r_grupo + " diz: " + r_bytes;
          System.out.println("");
          System.out.println(msg_formatada);
          System.out.print(Destino + ">> ");
          
        }
    };
    
    channel.basicConsume(user, true, consumer);
    channel_f.basicConsume(user + "_f", true, consumer_f);
    
    
    //  O programa entra em loop infitnito pra o envio de mensagens
    //  para fechar o programa basta digitar #fechar
    while (true){
      //  Aguarda e recebe a mensagem a ser enviada
      System.out.print(Destino + ">> ");
      //String message = entrada.nextLine();
      
      String texto = entrada.nextLine();
      if (texto.length() == 0){
        texto = " ";
      }
      String c = texto.substring(0,1);
      String sub;
      int qtd_char;
      
      switch (c){
        case "@" :
          System.out.println("Caso @");
          //sub = texto.substring(1,texto.length());
          Destino = texto.substring(1,texto.length());
          to_group = false;
          break;
        case "#" :
          //Casos para o grupo
          //Verificar se o grupo existe e/ou se tem permissão para enviar mensagens?
          System.out.println("Caso #");
          Destino = texto.substring(1,texto.length());
          to_group = true;
          break;
        case "!" :
          System.out.println("Caso !");
          String[] corte = texto.split("\\s+");
          System.out.println(corte[0]);
          switch(corte[0]){
            case "!addGroup":
              System.out.println("caso AddGroup");
              //channel.exchangeDeclare("logs", "fanout");
              System.out.println(corte[1]);
              channel.exchangeDeclare(corte[1], "fanout");
              //channel.queueBind(queueName, "logs", "");
              channel.queueBind(user,corte[1], "");
              
              break;
            case "!addUser":
              System.out.println("caso AddUser");
              channel.queueBind(corte[1],corte[2], "");
              
              break;
            case "!delFromGroup":
              System.out.println("caso delFromGroup");
              channel.queueUnbind(corte[1],corte[2], "");
              
              break;
            case "!removeGroup":
               System.out.println("caso removeGroup");
              channel.exchangeDelete(corte[1]);
              
              break;
            case "!upload":
              break;
            case "!listUsers":
              break;
            case "!listGroups":
              break;
              
            default:
              break;
          }
          break;
          
        default : //Aqui que a mensagem é enviada
          System.out.println("Caso DEFAULT");
          Date data = new Date();
           //NÂO ESTOU CONSEGUINDO SERIALIZAR AS MENSAGENS
          //SERIALIZANDO MENSAGENS
          /*
          msgProto.Conteudo.Builder cnt = msgProto.Conteudo.newBuilder();
          cnt.setTipo("text/plain");
          cnt.setCorpo(ByteString.copyFrom(texto.getBytes("UTF-8")));
          cnt.setNome("");
          
          msgProto.Mensagem.Builder msg = msgProto.Mensagem.newBuilder();
          msg.setEmissor(user);
          msg.setData(t_data.format(data));
          msg.setHora(t_hora.format(data));
          msg.setGrupo("");
          msg.setConteudo(cnt);
          
          msgProto.Mensagem mensagem = msg.build();
          
          byte[] buffer = mensagem.toByteArray();*/

          /* CRIAÇÂO DE ARQUIVOS
          FileOutputStream fos = new FileOutputStream(new File("json.bin"));
          fos.write(buffer);
          fos.close();
          //System.out.println("Contato escrito em formato binário no arquivo \"aluno.bin\"");
          
          String json = JsonFormat.printer().print(mensagem);
          
          fos = new FileOutputStream(new File("mensagem.json"));
          fos.write(json.getBytes());
          fos.close();
          */
          
          
          //Date data = new Date();
          //String txt = "(" + t_data.format(data) + " às " + t_hora.format(data) + ") " + user + " diz: " + texto;
          //System.out.println(txt);
          
          String caminhoAoArquivo = "/home/ubuntu/workspace/sistemas-distribuidos/Chat/arquivos/mistic.png"; 
          Path source = Paths.get(caminhoAoArquivo);
          String tipoMime = Files.probeContentType(source);
          System.out.println(tipoMime);
          /*Vizualizando o caminho do arquivo
          final String dir = System.getProperty("user.dir");
          System.out.println("current dir = " + dir); */
          msgProto.Conteudo.Builder cnt = msgProto.Conteudo.newBuilder();
          cnt.setTipo("text/plain");
          cnt.setCorpo(ByteString.copyFrom(texto.getBytes("UTF-8")));
          cnt.setNome("");
            
          msgProto.Mensagem.Builder msg = msgProto.Mensagem.newBuilder();
          msg.setEmissor(user);
          msg.setData(t_data.format(data));
          msg.setHora(t_hora.format(data));
          //msg.setGrupo(Destino);
          msg.setConteudo(cnt);
          
          if (to_group){
            //Se a mensagem for para um Grupo
            //String txt = "(" + t_data.format(data) + " às " + t_hora.format(data) + ") " + user + "#" + Destino + " diz: " + texto;
            /*
            msgProto.Conteudo.Builder cnt = msgProto.Conteudo.newBuilder();
            cnt.setTipo("text/plain");
            cnt.setCorpo(ByteString.copyFrom(texto.getBytes("UTF-8")));
            cnt.setNome("");
            
            msgProto.Mensagem.Builder msg = msgProto.Mensagem.newBuilder();
            msg.setEmissor(user);
            msg.setData(t_data.format(data));
            msg.setHora(t_hora.format(data));
            msg.setGrupo(Destino);
            msg.setConteudo(cnt);*/
            
            msg.setGrupo(Destino);
            
            msgProto.Mensagem mensagem = msg.build();
            
            byte[] buffer = mensagem.toByteArray();
            
            channel.basicPublish(Destino, "", null, buffer); 
          
          } else {/*
            //Se a mensagem for para um Usuário
            //String txt = "(" + t_data.format(data) + " às " + t_hora.format(data) + ") " + user + " diz: " + texto;
           
            msgProto.Conteudo.Builder cnt = msgProto.Conteudo.newBuilder();
            cnt.setTipo("text/plain");
            cnt.setCorpo(ByteString.copyFrom(texto.getBytes("UTF-8")));
            cnt.setNome("");
            
            msgProto.Mensagem.Builder msg = msgProto.Mensagem.newBuilder();
            msg.setEmissor(user);
            msg.setData(t_data.format(data));
            msg.setHora(t_hora.format(data));
            msg.setGrupo("");
            msg.setConteudo(cnt);
            */
            msg.setGrupo("");
            msgProto.Mensagem mensagem = msg.build();
            byte[] buffer = mensagem.toByteArray();
            //channel.basicPublish("", Destino, null, txt.getBytes("UTF-8")); 
            channel.basicPublish("", Destino, null, buffer); 
            
          }
          
          
          //channel.basicPublish("", Destino, null, texto.getBytes("UTF-8")); 
        
        
          break;
      }
      
      /* COMEÇA DAQUI
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
      //CODIGO NOVO ABAIXO
      } else {
        if (message.equals("!addGroup")){
          String NomeDoGrupo = message.substring(10,message.length());
          System.out.println("nome do grupo:" + NomeDoGrupo);
        }
      }TERMINA AQUI *
      /*
      if (message.substring(0,1).equals("!")){
        //caracter de comando
        String[] palavras = 
      }*/
      
      /*PARTE DOIS
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
      } FIM DOIS */
    }
  }
}