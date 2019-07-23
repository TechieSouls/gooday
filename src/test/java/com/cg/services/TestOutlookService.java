package com.cg.services;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.cg.CgApplication;
import com.cg.bo.CalendarSyncToken;
import com.cg.service.OutlookService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CgApplication.class)
@WebAppConfiguration
public class TestOutlookService {

	@Test
	public void test() {
		fail("Not yet implemented");
	}

	@Test
	public void testUnSubscribeFromCalendarService() {

		CalendarSyncToken calSycnToken = new CalendarSyncToken();
		calSycnToken.setSubscriptionId("795d40ec-e8d0-4cc2-93a9-e279d0f24710");
		calSycnToken.setRefreshToken("MCXg!1*ORg2BX7fW2ko903jbY4by2BX2yN0GfBvuzewV!nwhs*BK0gpDhzF5NAclnhrmgn0NuR0NQ8xFtxxNyYNgh0Bn3guNXMyHI*r!spRlub6eoTPppzXRhR6BbuJDvstPS*ts0N4l84fvZqwcrZu7sdmlpM0iWyy1yeeKAVmfT91docgPvH9aZAzkfE82V!W5Wpl8sIEnyPyg85*oRI3XaiLNoBIONHIwg1t!2OAunvfn*pqvBIqRkjIC94roRJCVrsAjoA0LQZMHN0giNoNyTfnQ5gV796*G8tRA774v12cNkDdhUXj0P*8mGCiWN4moHSFfYWokNLuv3lZWMllTwhzrtMtpnJGD6FhDboc!LXvtDycppHMW0BrLi40UOBBVgIfNkM43AdRm4ifXTsmXBOYnKNkSgG!Dl182DOW8aKAeb5tYEd08HhxqmKnt3JbDW4o2tZK4XenBHUNoUKqGyk6avQaJdlLNRKkglbury");
		
		OutlookService outlookService = new OutlookService();
		outlookService.unsubscribeFromOutlookService(calSycnToken);
		
	}
	
	
	@Test
	public void testRenewCalendarService() {

		CalendarSyncToken calSycnToken = new CalendarSyncToken();
		calSycnToken.setSubscriptionId("93566bbf-3c3d-40c7-8576-ee49e770741a");
		calSycnToken.setRefreshToken("MCbWaBfeHsK95SIxebPn1gLrOpzshsjED1ssBpvvA48MHzMf1sQfx5jD4Nb7cUVe08Oemqy7KWs5W8j!wXH!uf*jKEDSkDo*WlWrARJJLA!z4qyVv*BlHyHkSAoyxyxgF7O9hRBevPksUu5riaSZZSfeF21cg7iSw*o9mnrpIrdXQg1Sz18JXYSs4GezF!hhJW2HUcNgkZUHLcE1LH9XCRnycK6pbRTHMIu9uNoCz!n4nVYsd1!Z*aEV6czNboxkACaYIEd0OpaVBd7TgdG46YGTmgW9RaQGp9kI9xCUpEw5xqinyVmGpU0OuFKcCRZGuLgtPaT39IDPID2i9kIcC*K*yj*n6qONVLId4xXB6WvPelcTExzQga9FYh47C4kexVZpFa5pENHkrvZfZ8p6Hl3bCkBS5QD2DQZ5T*kOzYXp*F5TEP1OrlJ0QbjlCiY5CwWaodc6SvFs3VHiQgM7XIz3Da2dQ!RDSlWv5K1ndLok0");
		
		OutlookService outlookService = new OutlookService();
		outlookService.renewOutlookService(calSycnToken);
		
	}
}
