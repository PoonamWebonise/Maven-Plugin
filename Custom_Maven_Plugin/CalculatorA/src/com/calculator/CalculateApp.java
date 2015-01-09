package com.calculator;

import java.util.Scanner;
import com.calculatord.CalculatorImpl;

public class CalculateApp {
	
	public static void main(String[] args) {

		CalculatorImpl cal=new CalculatorImpl();
		Scanner sc=new Scanner(System.in);
		System.out.println("Enter value1:");
		int value1=sc.nextInt();
		System.out.println("Enter value1:");
		int value2=sc.nextInt();
		cal.addition(value1, value2);
		sc.close();
	}


}
