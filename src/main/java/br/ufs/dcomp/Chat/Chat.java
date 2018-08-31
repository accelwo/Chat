package br.ufs.dcomp.Chat;

import com.rabbitmq.client.*;

import java.io.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import java.util.*;
import java.util.Scanner;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.google.protobuf.util.JsonFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.protobuf.ByteString;


public class Chat {

  //private final static String QUEUE_NAME = "minha-fila";
  private final static DateFormat t_data = new SimpleDateFormat("dd/MM/yyyy");
  private final static DateFormat t_hora = new SimpleDateFormat("HH:mm:ss");
  public static boolean novo_destino;
  public static String Destino = new String("");
  public static boolean to_group = false;

  public static void main(String[] argv) throws Exception {
    
    Scanner entrada = new Scanner(System.in);
    
    ConnectionFactory factory = new ConnectionFactory();

    // Setando informações de conexão do servidor
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
   
    
    //declarações das filas 
    channel.queueDeclare(user, false, false, false, null);
    channel_f.queueDeclare(user + "_f", false, false, false, null);
    //System.out.println(" [*] Carregando Mensagens salvas!  [*]");


    //Receptor de mensagens de texto
    Consumer consumer = new DefaultConsumer(channel) {
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
            
          // Criação dos objetos protobuff
          msgProto.Mensagem rec_mensagem =  msgProto.Mensagem.parseFrom(body);
          msgProto.Conteudo rec_conteudo = rec_mensagem.getConteudo();
          
          // Receber os conteudos do protobuff
          String r_emissor = rec_mensagem.getEmissor();
          String r_data    = rec_mensagem.getData();
          String r_hora    = rec_mensagem.getHora();
          String r_grupo   = rec_mensagem.getGrupo();
          String r_tipo    = rec_conteudo.getTipo();
          String r_bytes   = rec_conteudo.getCorpo().toStringUtf8();
          String r_nome    = rec_conteudo.getNome();
          
          // Cria a formatação de mensagens
          String msg_formatada = "(" + r_data + " às " + r_hora + ") " + r_emissor;
          
          if (!r_grupo.equals("")) {
            msg_formatada = msg_formatada + "#";
          }
          msg_formatada = msg_formatada + r_grupo + " diz: " + r_bytes;
          
          // Escreve mensagem recebida
          System.out.println("");
          System.out.println(msg_formatada);
          System.out.print(Destino + ">> ");
          
        }
    };
    
     //Receptor de mensagens de arquivos _f
    Consumer consumer_f = new DefaultConsumer(channel_f) {   
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
            
          // Criação dos objetos Protobuff
          msgProto.Mensagem rec_mensagem =  msgProto.Mensagem.parseFrom(body);
          msgProto.Conteudo rec_conteudo = rec_mensagem.getConteudo();
          
          // Recebe os dados do Protobuff
          String r_emissor = rec_mensagem.getEmissor();
          String r_data    = rec_mensagem.getData();
          String r_hora    = rec_mensagem.getHora();
          String r_grupo   = rec_mensagem.getGrupo();
          String r_tipo    = rec_conteudo.getTipo();
          byte[] buff = rec_conteudo.getCorpo().toByteArray();
          String r_nome    = rec_conteudo.getNome();
          
          // Formata a mensagem
          String msg_formatada = "(" + r_data + " às " + r_hora + ") Arquivo \"" + r_nome + "\" recebido de @" + r_emissor;
          if (!r_grupo.equals("")) {
            msg_formatada = msg_formatada + "#" + r_grupo;
          } 
          System.out.println(msg_formatada);
          System.out.print(Destino + ">> ");
          
          //Cria o arquivo recebido
          FileOutputStream fileOS = new FileOutputStream(new File(r_nome));
          fileOS.write(buff);
          fileOS.close();
          
        }
    };
    
    
    // Receptor de mensagens
    channel.basicConsume(user, true, consumer);
    channel_f.basicConsume(user + "_f", true, consumer_f);
    
    
    //  O programa entra em loop infitnito pra o envio de mensagens
    //  para fechar o programa basta digitar !fechar
    while (true){
      //  Aguarda e recebe a mensagem a ser enviada
      System.out.print(Destino + ">> ");
      //String message = entrada.nextLine();
      
      String texto = entrada.nextLine();
      if (texto.length() == 0){
        texto = " ";
      }
      String c = texto.substring(0,1);
      
      switch (c){
        case "@" :
          //Mensagem para Usuário @
          Destino = texto.substring(1,texto.length());
          to_group = false;
          break;
        case "#" :
          //Mensagem para Grupo #
          Destino = texto.substring(1,texto.length());
          to_group = true;
          break;
        case "!" :
          String[] corte = texto.split("\\s+");
          //System.out.println(corte[0]);
          switch(corte[0]){
            case "!addGroup":
              //Formato: comando Nome_do_grupo
              
              //Criação o grupo e grupo_f
              channel.exchangeDeclare(corte[1], "fanout");
              channel.exchangeDeclare(corte[1]+"_f", "fanout");
              
              //Criando o Bind entre o usuário e o Grupo
              channel.queueBind(user,corte[1], "");
              channel.queueBind(user+"_f",corte[1]+"_f", "");
              
              break;
            case "!addUser":
              //Formato: comando Usuário Grupo
              channel.queueBind(corte[1],corte[2], "");
              channel.queueBind(corte[1]+"_f",corte[2]+"_f", "");
              
              break;
            case "!delFromGroup":
              //Formato: comando Usuário Grupo
              channel.queueUnbind(corte[1],corte[2], "");
              channel.queueUnbind(corte[1]+"_f",corte[2]+"_f", "");
              
              break;
            case "!removeGroup":
              //Formato: comando nome_do_grupo
              channel.exchangeDelete(corte[1]);
              channel.exchangeDelete(corte[1]+"_f");
              
              break;
            case "!upload":
              //Formato: comando dirétorio_de_arquivo
              
              // caminhoAoArquivo = "/home/ubuntu/workspace/sistemas-distribuidos/Chat/arquivos/mistic.png"; 
              String caminhoAoArquivo = corte[1];
              String[] separador = caminhoAoArquivo.split("/");
              String nome_do_arquivo = separador[separador.length - 1];
              Path source = Paths.get(caminhoAoArquivo);
              String tipoMime = Files.probeContentType(source);
              
              //Criação de variaveis que vão guardar os valores para previnir a não alteração da váriavel principal
              String th_Destino = Destino+"_f";
              String th_user = user;
              boolean th_toGroup = to_group;
              if (th_toGroup){
                System.out.println("[!]Enviando \""+ caminhoAoArquivo + "\" para #" + th_Destino + "[!]");
              } else {
                System.out.println("[!]Enviando \""+ caminhoAoArquivo + "\" para @" + th_Destino + "[!]");
              }
              

              
              //Thread que vai fazer o envio do arquivo
              Thread th = new Thread(new Runnable() {
                @Override
                  public void run() {
                    try{
                      
                      //  Armazeno os as variaveis que serão usadas para previrnir que elas sejam
                      //atualizadas enquanto envia o arquivo
                      Date th_data = new Date();
                      DateFormat th_dia = new SimpleDateFormat("dd/MM/yyyy");
                      DateFormat th_hora = new SimpleDateFormat("HH:mm:ss");
                      
                      msgProto.Conteudo.Builder th_cnt = msgProto.Conteudo.newBuilder();
                      msgProto.Mensagem.Builder th_msg = msgProto.Mensagem.newBuilder();
                      
                      //Leio os bytes do arquivo
                      byte[] bits = Files.readAllBytes(source);
                      th_cnt.setTipo(tipoMime);
                      th_cnt.setCorpo(ByteString.copyFrom(bits));
                      th_cnt.setNome(nome_do_arquivo);
                      
                      th_msg.setEmissor(th_user);
                      th_msg.setData(th_dia.format(th_data));
                      th_msg.setHora(th_hora.format(th_data));
                      th_msg.setConteudo(th_cnt);
                      
                      if (th_toGroup){
                        th_msg.setGrupo(th_Destino);
                        
                        msgProto.Mensagem th_mensagem = th_msg.build();
                        
                        byte[] th_buffer = th_mensagem.toByteArray();
                        
                        channel_f.basicPublish(th_Destino, "", null, th_buffer); 
                        System.out.println("");
                        System.out.println("[!]Arquivo \""+ caminhoAoArquivo + "\" foi enviado para #" + th_Destino + "[!]");
                        System.out.print(Destino + ">> ");
                      } else {
                        th_msg.setGrupo("");
                        
                        msgProto.Mensagem th_mensagem = th_msg.build();
                        byte[] th_buffer = th_mensagem.toByteArray();
                        channel_f.basicPublish("", th_Destino, null, th_buffer); 
                        System.out.println("");
                        System.out.println("[!]Arquivo \""+ caminhoAoArquivo + "\" foi enviado para @" + th_Destino + "[!]");
                        System.out.print(Destino + ">> ");
                      }
                      
                    } catch (IOException e){
                    }
                  }
                });
              
              //Verifica o tipoMime do arquivo
              if (tipoMime == null){
                System.out.println("[!]       Arquivo Incompátivel      [!]");
              } else {
                th.start();
              }

              break;
            case "!local":
              // Comando para escrever o diretorio que o java está sendo executado
              String dir = System.getProperty("user.dir");
              System.out.println("Caminho atual é = " + dir);
              
              break;
            case "!listUsers":
              //Comando Nome_do_grupo
              
              try{
                //Cria a url de solicitação
                URL url = new URL ("http://ec2-54-200-22-72.us-west-2.compute.amazonaws.com:15672/api/exchanges/%2F/" 
                        +corte[1]+"/bindings/source?columns=destination");
                        
                // Adiciona as informações de login e senha
                String encoding = Base64.getEncoder().encodeToString(("accel:@Accel27").getBytes("UTF-8"));
                
                // Conecta
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoOutput(true);
                conn.setRequestProperty  ("Authorization", "Basic " + encoding);
                InputStream content = (InputStream)conn.getInputStream();
                
                //Recebe o resultado
                BufferedReader in = new BufferedReader(new InputStreamReader(content));
                String linha = in.readLine();
                
                //Passa o resultado para um Json 
                JSONArray jsonA = new JSONArray(linha);
                
                //Verifica se o json está vazio
                if (jsonA.length() > 0){
                  
                  //Varre todos os objetos do json
                  System.out.print("Membros do grupo " + corte[1] +": ");
                  for (int i = 0; i < jsonA.length(); i++){
                    JSONObject jsonO = jsonA.getJSONObject(i);
                    String membro = jsonO.getString("destination");
                    System.out.print(membro + ", ");
                  }
                  System.out.println("");
                } else {
                  System.out.println("[!] Grupo inexistente e/ou vazio     [!]");
                }
                
                
              } catch (Exception e){
                e.printStackTrace();
              }
              
              
              break;
            case "!listGroups":
              
              //URL curl -i -u accel:@Accel27 "ec2-54-200-22-72.us-west-2.compute.amazonaws.com:15672/api/queues/%2F/_maria_/bindings?columns=source"
              
              //Comando 
              try{
                 //Cria a url de solicitação
                URL url = new URL ("http://ec2-54-200-22-72.us-west-2.compute.amazonaws.com:15672/api/queues/%2F/" 
                        +user+"/bindings?columns=source");
                
                String encoding = Base64.getEncoder().encodeToString(("accel:@Accel27").getBytes("UTF-8"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoOutput(true);
                conn.setRequestProperty  ("Authorization", "Basic " + encoding);
                InputStream content = (InputStream)conn.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(content));
                //String linha;
                String linha = in.readLine();
                JSONArray jsonA = new JSONArray(linha);
                if (jsonA.length() > 1){
                  System.out.print("Voce está no(s) grupo(s):  ");
                  for (int i = 1; i < jsonA.length(); i++){
                    JSONObject jsonO = jsonA.getJSONObject(i);
                    String membro = jsonO.getString("source");
                    System.out.print(membro + ", ");
                  }
                  System.out.println("");
                } else {
                  System.out.println("[!] Voce não participa de grupos     [!]");
                }
                
                
              } catch (Exception e){
                e.printStackTrace();
              }
              
              break;
            case "!fechar":
              // Finaliza o programa
              System.out.println("[!]       Programa Finalizado        [!]");
              System.exit(0);
              
              break;
              
            default:
              System.out.println("[!]       Comando Desconhecido       [!]");
              break;
          }
          break;
          
        default : //Aqui que a mensagem é enviada
        
          // Recebe a data do momento da mensagem
          Date data = new Date();
          
          //Cria o objeto Protobuff
          msgProto.Conteudo.Builder cnt = msgProto.Conteudo.newBuilder();
          cnt.setTipo("text/plain");
          cnt.setCorpo(ByteString.copyFrom(texto.getBytes("UTF-8")));
          cnt.setNome("");
            
          msgProto.Mensagem.Builder msg = msgProto.Mensagem.newBuilder();
          msg.setEmissor(user);
          msg.setData(t_data.format(data));
          msg.setHora(t_hora.format(data));
          msg.setConteudo(cnt);
          
          // Verifica se a mensagem é para um grupo
          if (to_group){
            msg.setGrupo(Destino);
            
            //Cria a mensagem
            msgProto.Mensagem mensagem = msg.build();
            
            //Prepara a mensagem para ser enviada (bytes)
            byte[] buffer = mensagem.toByteArray();
            
            // Envia a mensagem
            channel.basicPublish(Destino, "", null, buffer); 
          
          } else {
            msg.setGrupo("");
            //Cria a mensagem
            msgProto.Mensagem mensagem = msg.build();
            byte[] buffer = mensagem.toByteArray();
             // Envia a mensagem
            channel.basicPublish("", Destino, null, buffer); 
          }
          
          break;
      }
    }
  }
}