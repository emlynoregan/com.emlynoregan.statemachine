package com.emlynoregan.statemachinetests;

import static org.junit.Assert.*;

import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.emlynoregan.statemachine.StateMachine;
import com.emlynoregan.statemachine.StateMachine.Condition;
import com.emlynoregan.statemachine.StateMachine.IStateChange;
import com.emlynoregan.statemachine.StateMachine.State;

public class SimpleStateMachineTests {

	boolean lnewStateIsStop;
	String lerrorMessage;
	
	@Test
	public void test() throws Exception
	{
		ConcurrentHashMap<SimpleEntry<State, Condition>, State> ltransitions 
			= new ConcurrentHashMap<SimpleEntry<State,Condition>, StateMachine.State>();

		lnewStateIsStop = false;
		lerrorMessage = null;
		
		final CountDownLatch llatch = new CountDownLatch(2);
		final StateMachine.State lstartState = new StateMachine.State("start");
		final StateMachine.State lstopState = new StateMachine.State("stop");
		
		ltransitions.put(
			new SimpleEntry<StateMachine.State, StateMachine.Condition>(lstartState, StateMachine.getProceedCondition()), 
			lstopState
			);
		
		StateMachine s = new StateMachine
		(
			ltransitions,
			lstartState,
			lstopState,
			new IStateChange() {
				@Override
				public void OnNewState(State newState) {
					if (newState.equals(lstopState))
					{
						lerrorMessage = null;
						lnewStateIsStop = true;
					}
					else
					{
						lerrorMessage = String.format("Unexpected state %s", newState.toString());
					}
					llatch.countDown();
				}
			}
		);
		
		s.Start();
		try
		{
			s.RaiseCondition(StateMachine.getProceedCondition());
			
			llatch.await(1000, TimeUnit.MILLISECONDS);
			
			assertTrue(lerrorMessage, lnewStateIsStop);
		}
		finally
		{
			s.Dispose();
		}
	}

}
