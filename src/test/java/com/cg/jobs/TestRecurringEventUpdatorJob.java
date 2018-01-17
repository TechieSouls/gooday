package com.cg.jobs;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.cg.CgApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CgApplication.class)
@WebAppConfiguration
public class TestRecurringEventUpdatorJob extends TestCase{

	@Autowired
	RecurringEventUpdaterJob recurringEventUpdaterJob;
	
	@Autowired
	RecurringPatternUpdaterJob recurringPatternUpdaterJob;;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRunRecurringEventUpdatorJob() {
		recurringEventUpdaterJob.runRecurringEventUpdatorJob();
	}
	
	@Test
	public void testRunRecurringPatternUpdaterJob() {
		recurringPatternUpdaterJob.runRecurringPatternUpdaterJob();
	}

}
