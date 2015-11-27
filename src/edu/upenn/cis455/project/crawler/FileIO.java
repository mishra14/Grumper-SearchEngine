package edu.upenn.cis455.project.crawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileIO
{
	public static synchronized void writeURL(String url, int idx){
		PrintWriter out;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter("./"+idx+".txt", true)));
			out.println(url);
			out.close();
		} catch (IOException e) {
			
		}
	}
	
//	public static <T> void write(T t){
//		PrintWriter out;
//		try
//		{
//			out = new PrintWriter(new BufferedWriter(new FileWriter("./urls.txt",true)));
//			out.println(t);
//			out.close();
//		}
//		catch (IOException e)
//		{
//			System.out.println("Could not write to file: "+e);
//		}
//	}
//	
//	public static void main(String args[]) throws IOException{
//		
//		Queue<String> q = QueueDA.getQueue();
//		BufferedReader br = q.br;
//		
//		System.out.println(br.readLine());
//		System.out.println(br.readLine());
//		System.out.println(br.readLine());
//		
//		QueueDA.putQueue(q, new Date());
//		
////		FileInputStream is = new FileInputStream("./URLFile");
////		InputStreamReader isr = new InputStreamReader(is);
////		BufferedReader br = new BufferedReader(isr);
////		String line;
////		while((line = br.readLine())!=null){
////			System.out.println(line);
////		}
////		br.close();
////		isr.close();
////		is.close();
//	}
}
