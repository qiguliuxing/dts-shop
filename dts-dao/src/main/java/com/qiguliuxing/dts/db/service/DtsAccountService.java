package com.qiguliuxing.dts.db.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsAccountTraceMapper;
import com.qiguliuxing.dts.db.dao.DtsUserAccountMapper;
import com.qiguliuxing.dts.db.dao.DtsUserMapper;
import com.qiguliuxing.dts.db.dao.ex.AccountMapperEx;
import com.qiguliuxing.dts.db.domain.DtsAccountTrace;
import com.qiguliuxing.dts.db.domain.DtsAccountTraceExample;
import com.qiguliuxing.dts.db.domain.DtsOrder;
import com.qiguliuxing.dts.db.domain.DtsUserAccount;
import com.qiguliuxing.dts.db.domain.DtsUserAccountExample;
import com.qiguliuxing.dts.db.domain.DtsUserExample;

@Service
public class DtsAccountService {

	private final static Log logger = LogFactory.getLog(DtsAccountService.class);

	@Resource
	private DtsUserAccountMapper userAccountMapper;

	@Resource
	private DtsAccountTraceMapper accountTraceMapper;

	@Resource
	private AccountMapperEx accountMapperEx;

	@Resource
	private DtsUserMapper userMapper;

	public DtsUserAccount findShareUserAccountByUserId(Integer shareUserId) {

		DtsUserAccountExample example = new DtsUserAccountExample();
		example.or().andUserIdEqualTo(shareUserId);
		List<DtsUserAccount> accounts = userAccountMapper.selectByExample(example);
		// Assert.state(accounts.size() < 2, "同一个用户存在两个账户");
		if (accounts.size() == 0) {
			return null;
		}
		return accounts.get(0);
	}

	public List<Integer> findAllSharedUserId() {
		return accountMapperEx.getShareUserId();
	}

	private String getRandomNum(Integer num) {
		String base = "0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < num; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void setSettleMentAccount(Integer sharedUserId, String prevMonthEndDay) throws Exception {
		// 1.获取用户的代理订单代理金额
		String endTime = prevMonthEndDay + " 23:59:59";
		String startTime = prevMonthEndDay.substring(0, 7) + "-01 00:00:00";
		BigDecimal totalSettleMoney = accountMapperEx.getLastMonthSettleMoney(sharedUserId, startTime, endTime);
		logger.info("代理用户编号： {" + sharedUserId + "},日期：" + startTime + " - " + endTime + ",获取佣金: " + totalSettleMoney
				+ "元");

		// 更新订单结算状态
		accountMapperEx.setLastMonthOrderSettleStaus(sharedUserId, startTime, endTime);

		Integer settlementRate = 4;
		if (totalSettleMoney.compareTo(new BigDecimal("0")) == 0) {// 如果该用户未产生推荐单，则降低结算比例
			settlementRate = 2;
		}

		// 获取用户账户信息并更新记录
		DtsUserAccount account = this.findShareUserAccountByUserId(sharedUserId);

		// 新增账户跟踪表，添加结算跟踪记录
		DtsAccountTrace trace = new DtsAccountTrace();
		trace.setAmount(totalSettleMoney);
		trace.setRemainAmount(account == null ? totalSettleMoney : account.getTotalAmount().add(totalSettleMoney));
		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd");
		String now = df.format(LocalDate.now());
		String traceSn = now + getRandomNum(6);
		trace.setTraceSn(traceSn);
		trace.setTraceTime(LocalDateTime.now());
		trace.setType(0);
		trace.setUserId(sharedUserId);
		accountTraceMapper.insert(trace);

		if (account == null) { // 则创建
			account = new DtsUserAccount();
			account.setCreateTime(LocalDateTime.now());
			account.setModifyTime(LocalDateTime.now());
			account.setRemainAmount(totalSettleMoney);
			account.setSettlementRate(settlementRate);
			account.setTotalAmount(totalSettleMoney);
			userAccountMapper.insert(account);
		} else {
			account.setRemainAmount(account.getRemainAmount().add(totalSettleMoney));
			account.setTotalAmount(account.getTotalAmount().add(totalSettleMoney));
			account.setModifyTime(LocalDateTime.now());
			account.setSettlementRate(settlementRate);
			userAccountMapper.updateByPrimaryKeySelective(account);
		}

		// 再次验证是否都已经结算
		BigDecimal vaildSettleMoney = accountMapperEx.getLastMonthSettleMoney(sharedUserId, startTime, endTime);
		if (vaildSettleMoney.compareTo(new BigDecimal("0")) > 0) {
			logger.error("错误：结算过程有误，请联系管理员排查 ，代理用户编号： {" + sharedUserId + "},日期：" + startTime + " - " + endTime
					+ ",获取佣金: " + totalSettleMoney + "元");
			throw new Exception("结算过程有误，请联系管理员排查");
		}
	}

	/**
	 * 统计某个用户时间段内的结算金额
	 * 
	 * @param userId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public BigDecimal getMonthSettleMoney(Integer sharedUserId, String startTime, String endTime) {
		BigDecimal staticSettleMoney = accountMapperEx.staticMonthSettleMoney(sharedUserId, startTime, endTime);
		if (staticSettleMoney == null || staticSettleMoney.compareTo(new BigDecimal("0")) == 0) {// 如果该用户未产生推荐单，则降低结算比例
			staticSettleMoney = new BigDecimal(0.00);
		}
		return staticSettleMoney;
	}

	public Map<String, Object> getStatistics(Integer sharedUserId, int dayAgo) {
		Map<String, Object> result = new HashMap<String, Object>();
		LocalDateTime startTime = LocalDateTime.now().minusDays(dayAgo);

		DtsUserExample example = new DtsUserExample();
		example.or().andDeletedEqualTo(false).andShareUserIdEqualTo(sharedUserId)
				.andAddTimeGreaterThanOrEqualTo(startTime);
		Long userCnt = (Long) userMapper.countByExample(example);

		Long orderCnt = accountMapperEx.countOrderSharedUser(sharedUserId, startTime);
		BigDecimal orderSettleAmt = accountMapperEx.sumOrderSettleAmtSharedUser(sharedUserId, startTime);
		if (orderSettleAmt == null) {
			orderSettleAmt = new BigDecimal(0.00);
		}
		BigDecimal finalSettleAmt = orderSettleAmt; // 默认就是设置的结算价格
		result.put("userCnt", userCnt);
		result.put("orderCnt", orderCnt);
		result.put("orderSettleAmt", orderSettleAmt);
		result.put("finalSettleAmt", finalSettleAmt);
		return result;
	}

	public List<DtsOrder> querySettlementOrder(Integer sharedUserId, List<Short> orderStatus,
			List<Short> settlementStatus, Integer page, Integer size) {

		String conditionSql = null;
		if (orderStatus != null) {
			conditionSql = "";
			for (Short orderStatu : orderStatus) {
				conditionSql += "," + orderStatu;
			}
			conditionSql = "and o.order_status in (" + conditionSql.substring(1) + ") ";
		}
		if (settlementStatus != null && settlementStatus.size() == 1) {
			conditionSql = conditionSql + " and o.settlement_status =" + settlementStatus.get(0) + " ";
		}

		PageHelper.startPage(page, size);
		return accountMapperEx.querySettlementOrder(sharedUserId, conditionSql);
	}

	public List<DtsAccountTrace> queryAccountTraceList(Integer userId, Integer page, Integer size) {
		DtsAccountTraceExample example = new DtsAccountTraceExample();
		example.setOrderByClause(DtsAccountTrace.Column.traceTime.desc());
		DtsAccountTraceExample.Criteria criteria = example.or();
		criteria.andUserIdEqualTo(userId);
		PageHelper.startPage(page, size);
		return accountTraceMapper.selectByExample(example);
	}

	/**
	 * 新增申请提现记录
	 * 
	 * @param userId
	 * @param applyAmt
	 */
	public void addExtractRecord(Integer userId, BigDecimal applyAmt, String mobile, String smsCode,
			BigDecimal remainAmount) {

		DtsAccountTrace record = new DtsAccountTrace();
		record.setAmount(applyAmt);
		record.setMobile(mobile);
		record.setRemainAmount(remainAmount);
		record.setSmsCode(smsCode);

		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd");
		String now = df.format(LocalDate.now());
		String traceSn = now + getRandomNum(6);
		record.setTraceSn(traceSn);

		record.setTraceTime(LocalDateTime.now());
		record.setType(1);// 申请中..
		record.setUserId(userId);
		accountTraceMapper.insert(record);
	}

	public void add(DtsUserAccount userAccount) {
		userAccount.setCreateTime(LocalDateTime.now());
		userAccount.setModifyTime(LocalDateTime.now());
		userAccountMapper.insert(userAccount);
	}

}
