package com.avaje.tests.basic;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;


public class TestLimitQuery extends BaseTestCase {

	@Test
	public void testNothing() {
		
	}

	@Test
	public void testLimitWithMany() {
		rob();
		rob();
	}

	@Test
	public void testMaxRowsZeroWithFirstRow() {
		ResetBasicData.reset();

		SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
		boolean h2Db = "h2".equals(server.getDatabasePlatform().getName());

		Query<Order> query = Ebean.find(Order.class)
			.setAutofetch(false)
			.fetch("details")
			.where().gt("details.id", 0)
			.setMaxRows(0)
			.setFirstRow(3);

		query.findList();

		String sql = query.getGeneratedSql();
		boolean hasLimit = sql.contains("limit 0");
		boolean hasOffset = sql.contains("offset 3");

		if (h2Db) {
			Assert.assertTrue(hasLimit);
			Assert.assertTrue(hasOffset);
		}
	}

	@Test
	public void testMaxRowsWithFirstRowZero() {
		ResetBasicData.reset();

		SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
		boolean h2Db = "h2".equals(server.getDatabasePlatform().getName());

		Query<Order> query = Ebean.find(Order.class)
			.setAutofetch(false)
			.fetch("details")
			.where().gt("details.id", 0)
			.setMaxRows(3)
			.setFirstRow(0);

		query.findList();

		String sql = query.getGeneratedSql();
		boolean hasLimit = sql.contains("limit 3");
		boolean hasOffset = sql.contains("offset");

		if (h2Db) {
			Assert.assertTrue(sql, hasLimit);
			Assert.assertFalse(sql, hasOffset);
		}
	}

	@Test
	public void testDefaults() {
		ResetBasicData.reset();

		SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
		boolean h2Db = "h2".equals(server.getDatabasePlatform().getName());

		Query<Order> query = Ebean.find(Order.class)
			.setAutofetch(false)
			.fetch("details")
			.where().gt("details.id", 0)
			.query();

		query.findList();

		String sql = query.getGeneratedSql();
		boolean hasLimit = sql.contains("limit");
		boolean hasOffset = sql.contains("offset");

		if (h2Db) {
			Assert.assertFalse(hasLimit);
			Assert.assertFalse(hasOffset);
		}
	}

	private void rob() {
		ResetBasicData.reset();
		
		SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
		boolean h2Db = "h2".equals(server.getDatabasePlatform().getName());
		
		Query<Order> query = Ebean.find(Order.class)
			.setAutofetch(false)
			.fetch("details")
			.where().gt("details.id", 0)
			.setMaxRows(10);
			//.findList();
		
		List<Order> list = query.findList();
		
		Assert.assertTrue("sz > 0", list.size() > 0);

		String sql = query.getGeneratedSql();
		boolean hasDetailsJoin = sql.contains("join o_order_detail");
		boolean hasLimit = sql.contains("limit 10");
		boolean hasSelectedDetails = sql.contains("od.id,");
		boolean hasDistinct = sql.contains("select distinct");
		
		Assert.assertTrue(hasDetailsJoin);
		Assert.assertFalse(hasSelectedDetails);
		Assert.assertTrue(hasDistinct);
		if (h2Db){
			Assert.assertTrue(hasLimit);
		}
		
		query = Ebean.find(Order.class)
			.setAutofetch(false)
			.fetch("details")
			.setMaxRows(10);
		
		query.findList();
		
		sql = query.getGeneratedSql();
		hasDetailsJoin = sql.contains("left outer join o_order_detail");
		hasLimit = sql.contains("limit 10");
		hasSelectedDetails = sql.contains("od.id");
		hasDistinct = sql.contains("select distinct");

		Assert.assertFalse("no join with maxRows",hasDetailsJoin);
		Assert.assertFalse(hasSelectedDetails);
		Assert.assertFalse(hasDistinct);
		if (h2Db){
			Assert.assertTrue(hasLimit);			
		}
	}
}
