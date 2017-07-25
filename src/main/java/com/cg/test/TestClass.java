package com.cg.test;

import java.util.HashMap;
import java.util.Map;

public class TestClass {

	public static void main1(String[] args) {
		/*LinkedList<String> linkedList = new LinkedList<>();
		linkedList.add("1");
		linkedList.add("2");
		linkedList.push("4");
		PriorityQueue<String> g = new PriorityQueue<>();
		
		linkedList.add("3");
		//linkedList.removeLast();
		System.out.println(linkedList);
		System.out.println(linkedList.offerFirst("3"));
		System.out.println(linkedList);
		List names = new ArrayList();
		names.add("rest");
		names.stream().forEach(System.out::println);;
		
		try{
		Thread.sleep(1000);
		}catch(Exception e){
			
		}catch(Error r){
			
		}catch(Throwable r){
			
		}
		
		 math c= (a,b)->a+b/a;
		System.out.println(c.op(4, 5));*/
		TestClass a = new TestClass();
		Map<Employee, Integer> map = new HashMap<>();
		Employee employee = a.new Employee(23);
		map.put(employee, 1);
		map.put(a.new Employee(23), 2);
		System.out.println(map);
		
		}
	
	public class Employee{
		
		public Employee(int id){
			this.id = id;
		}
		
		public int id;
		
		@Override
		public int hashCode() {
			// TODO Auto-generated method stub
		return id;
		}
		
		@Override
		public boolean equals(Object obj) {
			// TODO Auto-generated method stub
			return super.equals(obj);
		}
	}
	interface math {
		public int op(int a,int b);
	}
}
