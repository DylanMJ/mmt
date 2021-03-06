package lemon.web.customer.action;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import lemon.shared.customer.bean.Customer;
import lemon.shared.customer.mapper.CustomerMapper;
import lemon.shared.entity.Status;
import lemon.web.base.AdminNavAction;
import lemon.web.system.bean.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * 客户信息管理
 * 
 * @author lemon
 * @version 1.0
 * 
 */
@Controller
@RequestMapping("/customer/information")
public final class CustInfoAction extends AdminNavAction {
	@Autowired
	private CustomerMapper customerMapper;

	/**
	 * 显示客户信息列表首页
	 * @return
	 */
	@RequestMapping("list")
	public String list() {
		return "redirect:list/1";
	}
	
	/**
	 * 分页显示客户信息列表
	 * @param page
	 * @param user_name
	 * @param session
	 * @return
	 */
	@RequestMapping("list/{page}")
	public ModelAndView list(@PathVariable("page") int page, String cust_name,
			HttpSession session) {
		//获取Main视图名称
		String mainViewName = getMainViewName(Thread.currentThread().getStackTrace()[1].getMethodName());
		if(null == mainViewName)
			sendNotFoundError();
		//获取用户角色
		User user = (User) session.getAttribute(TOKEN);
		//获取导航条数据
		Map<String, Object> resultMap = buildNav(user.getRole_id());
		//获取Main数据
		List<Customer> custList = customerMapper.getCustomerList((page - 1) * PAGESIZE, PAGESIZE);
		obtainServices(custList);
		int rsCnt = customerMapper.getCustCnt();
		resultMap.put("mainViewName", mainViewName);
		resultMap.put("custList", custList);
		resultMap.put("rsCnt", rsCnt);
		resultMap.put("currentPage", page);
		resultMap.put("PAGESIZE", PAGESIZE);
		return new ModelAndView(VIEW_MANAGE_HOME_PAGE, "page", resultMap);
	}
	

	/**
	 * 保存客户信息
	 * @param cust
	 * @param br
	 * @return
	 */
	@RequestMapping(value = "save", method = RequestMethod.POST, produces = "text/html;charset=UTF-8")
	@ResponseBody
	public String save(@Valid Customer cust,BindingResult br) {
		if(cust == null)
			return sendJSONError("客户信息保存失败：信息不全。");
		if(br.hasErrors())
			return sendJSONError(br.getFieldError().getDefaultMessage());
		int result = 0;
		if(cust.getCust_id() <= 0){
			cust.setStatus(Status.AVAILABLE);
			result = customerMapper.addCustomer(cust);
		}else
			result = customerMapper.updateCustomer(cust);
		if(result != 0)
			return sendJSONMsg("客户信息保存成功。");
		else
			return sendJSONError("客户信息保存失败。");
	}
	
	/**
	 * 删除客户信息
	 * @param cust_id
	 * @return
	 */
	@RequestMapping(value = "delete", method = RequestMethod.POST, produces = "text/html;charset=UTF-8")
	@ResponseBody
	public String delete(int cust_id) {
		if (cust_id <= 0)
			return sendJSONError("客户信息删除失败： 客户不存在。");
		int result = customerMapper.delete(cust_id);
		if (0 != result)
			return sendJSONMsg("客户信息删除成功。");
		else
			return sendJSONError("客户信息删除失败。");
	}
	
	/**
	 * 显示添加或者编辑客户信息的页面
	 * @param cust_id
	 * @return
	 */
	@RequestMapping(value="add-edit-page")
	public ModelAndView addOrEditPage(int cust_id) {
		Customer cust = null;
		if(cust_id > 0)
			cust = customerMapper.getCustomer(cust_id);
		if(null == cust)
			cust = new Customer();
		return new ModelAndView(getAddEditView(), "cust", cust);
	}

	@Override
	public String getMenuURL() {
		return "customer/information";
	}
	
	/**
	 * 查询服务开通情况
	 * @param list
	 */
	private void obtainServices(List<Customer> list){
		for (Customer customer : list) {
			customer.setServices(customerMapper.getServices(customer.getCust_id()));
		}
	}

}
