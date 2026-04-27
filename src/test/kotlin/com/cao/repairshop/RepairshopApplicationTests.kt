package com.cao.repairshop

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@Disabled("Requires a physical database connection not available in this environment")
@ActiveProfiles("test")
@SpringBootTest
class RepairshopApplicationTests {

	@Test
	fun contextLoads() {
	}

}
