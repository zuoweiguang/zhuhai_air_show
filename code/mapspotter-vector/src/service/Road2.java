package service;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hbase.async.GetRequest;
import org.hbase.async.KeyValue;

import com.navinfo.pool.HBasePool;

public class Road2 extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7951351347233842590L;

	/**
	 * Constructor of the object.
	 */
	public Road2() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doPost(request, response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to
	 * post.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("application/x-protobuf");

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods",
				"POST, GET, OPTIONS, DELETE,PUT");

		String url = request.getRequestURI();

//		String[] splits = url.split("/");
//
//		int len = splits.length;
//		
//		String type = splits[len - 1].replace(".pbf", "");
//
//		int y = Integer.parseInt(splits[len - 2]);
//
//		int x = Integer.parseInt(splits[len - 3]);
	
		GetRequest get = new GetRequest("track_link", "3372_1551");
		get = get.family("data".getBytes());
		get = get.qualifier("didi");
		try {
			ArrayList<KeyValue> list = HBasePool.getClient().get(get)
					.joinUninterruptibly();

			if (list != null && list.size() > 0) {
				for (KeyValue kv : list) {
					response.getOutputStream().write(kv.value());
					return;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException
	 *             if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
