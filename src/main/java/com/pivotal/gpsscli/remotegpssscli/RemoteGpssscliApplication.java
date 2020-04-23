package com.pivotal.gpsscli.remotegpssscli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import java.io.*;
import java.util.*;
import com.jcraft.jsch.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class RemoteGpssscliApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(RemoteGpssscliApplication.class, args);
	}

	@Override
	public void run(String[] args) throws Exception {
		FileInputStream in;
		Properties sshProperties = new Properties();

		try {
			in = new FileInputStream("remote.properties");
			sshProperties.load(in);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String host = sshProperties.getProperty("host");
		String username = sshProperties.getProperty("username");
		String password = sshProperties.getProperty("password");
		String command = sshProperties.getProperty("command");

		System.out.println("executing: " + command);


		executeCommand(host, username, password, command);


	}

	public void executeCommand(String host, String user, String password, String command) {


		JSch jsch = new JSch();

		Session session;
		try {

			// Open a Session to remote SSH server and Connect.
			// Set User and IP of the remote host and SSH port.
			session = jsch.getSession(user, host, 22);
			// When we do SSH to a remote host for the 1st time or if key at the remote host
			// changes, we will be prompted to confirm the authenticity of remote host.
			// This check feature is controlled by StrictHostKeyChecking ssh parameter.
			// By default StrictHostKeyChecking  is set to yes as a security measure.
			session.setConfig("StrictHostKeyChecking", "no");
			//Set password
			session.setPassword(password);
			session.connect();

			// create the execution channel over the session
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
			// Set the command to execute on the channel and execute the command
			channelExec.setCommand(command);
			channelExec.connect();

			// Get an InputStream from this channel and read messages, generated
			// by the executing command, from the remote side.
			InputStream in = channelExec.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}

			// Command execution completed here.

			// Retrieve the exit status of the executed command
			int exitStatus = channelExec.getExitStatus();
			if (exitStatus > 0) {
				System.out.println("Remote script exec error! " + exitStatus);
			}
			//Disconnect the Session
			session.disconnect();
		} catch (JSchException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}