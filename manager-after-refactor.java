package com.refactor.worksheet.manager;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.validator.DynaValidatorActionForm;

import com.refactor.common.Constants;
import com.refactor.common.i18n.I18NContext;
import com.refactor.common.services.ServicesUtil;
import com.refactor.common.util.CommonUtil;
import com.refactor.creditapplication.action.CreditAppDispatchAction;
import com.refactor.creditapplication.dataobject.CreditApplicationDO;
import com.refactor.creditapplication.util.TOFieldTransformations;
import com.refactor.creditapplication.util.WorksheetUtil;
import com.refactor.domain.User;
import com.refactor.esign.managers.DisplayManager;
import com.refactor.esign.managers.RequestManager;
import com.refactor.framework.exception.newex.ApplicationException;
import com.refactor.framework.exception.newex.SystemException;
import com.refactor.worksheet.base.AbstractManager;
import com.refactor.worksheet.constant.WorksheetConstants;
import com.refactor.worksheet.doutil.WorksheetContractTermsDAUtil;
import com.refactor.worksheet.factory.ManagerFactory;

/**
 * @author Adam DePollo 5/14/2020
 *
 */
public class SaveEsignDocumentsAndViewSignStatusManager extends AbstractManager{
	
	private DisplayManager displayManager = new DisplayManager();
	private WorksheetUtil worksheetUtil = new WorksheetUtil();
	private ServicesUtil servicesUtil = new ServicesUtil();
	private WorksheetContractTermsDAUtil worksheetContractTermsDAUtil = new WorksheetContractTermsDAUtil();
	private RequestManager requestManager = new RequestManager();
	private ManagerFactory managerFactory = new ManagerFactory();
	
	private DynaValidatorActionForm caForm;
	private DynaValidatorActionForm dForm;
	private HttpServletRequest request;
	private String dealerState;
	private String finalIntRate;
	private CreditAppDispatchAction subject;
	private I18NContext context;
	private String printLang;
	private String caOid;
	private HttpSession session;
	private CreditApplicationDO creditApp;
	private Long dlrOid;
	private User user;
	private String financeSourceId;
	private Map dynaFieldMap;
	private String fsOid;
	private String printInputXml;
	private Set<UUID> documentContextIdList;
	
	public void initializeDataValues(DynaValidatorActionForm dForm, HttpServletRequest request, CreditAppDispatchAction subject) throws SystemException, ApplicationException {
		setRequest(request);
		setSubject(subject);
		setdForm(dForm);
		setCaForm();
		setDealerState();
		setFinalIntRate();
		setContext();
		setPrintLang();
		setCaOid();
		setSession();
		setCreditApp();
		setDlrOid();
		setUser();
		setFinanceSourceId();
		setDynaFieldMap();
		setFsOid();
		setDocumentContextIdList();
		setPrintInputXml();
		copyFsDetailsFromCreditAppToWorksheet();
        setEsignPackageTotalDocumentSize();
        setUserTimeZone();
        saveFinalInterestRateToWorksheet();
        populateDefaultWorksheetAndCreditAppFields();
        setWorksheetTaxAndFeeDescriptions();
        setClientIdValues();
	}

	public DynaValidatorActionForm getCaForm() {
		return caForm;
	}

	public void setCaForm() {
		this.caForm = worksheetUtil.getCreditApplication(request);
	}

	public DynaValidatorActionForm getdForm() {
		return dForm;
	}

	public void setdForm(DynaValidatorActionForm form) {
		this.dForm = form;
	}

	public WorksheetUtil getWorksheetUtil() {
		return worksheetUtil;
	}

	public void setWorksheetUtil(WorksheetUtil worksheetUtil) {
		this.worksheetUtil = worksheetUtil;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest httpServletRequest) {
		this.request = httpServletRequest;
	}

	public String getDealerState() {
		return dealerState;
	}

	public void setDealerState() {
		this.dealerState = worksheetUtil.getDealerStateNonStatic(request);
	}

	public String getFinalIntRate() {
		return finalIntRate;
	}

	public void setFinalIntRate() {
		this.finalIntRate = (String) dForm.getMap().get(WorksheetConstants.ContractProgram.FINAL_INTEREST_RATE);
	}

	public CreditAppDispatchAction getSubject() {
		return subject;
	}

	public void setSubject(CreditAppDispatchAction subject) {
		this.subject = subject;
	}

	public I18NContext getContext() {
		return context;
	}

	public void setContext() {
		this.context = (I18NContext) request.getSession(false).getAttribute(Constants.INTERNATIONALIZATION_CONTEXT);
	}

	public String getPrintLang() {
		return printLang;
	}

	public void setPrintLang() {
		this.printLang = (String) dForm.get(WorksheetConstants.ContractContextConstants.PRINT_LANGUAGE);
	}

	public String getCaOid() {
		return caOid;
	}

	public void setCaOid() {
		this.caOid = String.valueOf(caForm.get(WorksheetConstants.CreditApp.Fields.OID));
	}

	public HttpSession getSession() {
		return session;
	}

	public void setSession() {
		this.session = request.getSession(false);
	}

	public DisplayManager getDisplayManager() {
		return displayManager;
	}

	public void setDisplayManager(DisplayManager displayManager) {
		this.displayManager = displayManager;
	}

	public CreditApplicationDO getCreditApp() {
		return creditApp;
	}

	public void setCreditApp(CreditApplicationDO creditApp) {
		this.creditApp = creditApp;
	}
	
	public void setCreditApp() {
		this.creditApp = displayManager.getCreditApplicationDO(caOid);
	}

	public Long getDlrOid() {
		return dlrOid;
	}

	public void setDlrOid(Long dlrOid) {
		this.dlrOid = dlrOid;
	}
	
	public void setDlrOid() {
		this.dlrOid = (Long) session.getAttribute(Constants._DEALER_OID);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public void setUser() throws SystemException {
		this.user = CommonUtil.getAuditUser(request);
	}

	public String getFinanceSourceId() {
		return financeSourceId;
	}

	public void setFinanceSourceId(String financeSourceId) {
		this.financeSourceId = financeSourceId;
	}
	
	public void setFinanceSourceId() {
		this.financeSourceId = (String) dForm.get(WorksheetConstants.ContractContextConstants.FNC_SRC_ID);
	}

	public Map getDynaFieldMap() {
		return dynaFieldMap;
	}
	
	public void setDynaFieldMap(Map map) {
		this.dynaFieldMap = map;
	}
	
	public void setDynaFieldMap() {
		this.dynaFieldMap = worksheetUtil.copyMapAndDynaFieldsNonStatic(dForm);
	}

	public String getFsOid() {
		return fsOid;
	}
	
	public void setFsOid(String fsOid) {
		this.fsOid = fsOid;
	}
	public void setFsOid() {
		this.fsOid = (String) dynaFieldMap.get(WorksheetConstants.ContractContextConstants.FNC_SRC_OID);
	}

	public void setDocumentContextIdList(Set<UUID> documentContextIdList) {
		this.documentContextIdList = documentContextIdList;
	}
	
	public void setDocumentContextIdList() throws SystemException {
		this.documentContextIdList = requestManager.getDocumentContextIds(managerFactory.createWorksheetWrapper().getCreditApplicationDocMap(creditApp.getCrdtAplcnOid(), requestManager.getDocumentTypeArray()));
        
	}

	public String getPrintInputXml() {
		return printInputXml;
	}
	
	public void setPrintInputXml(String printInputXml) {
		this.printInputXml = printInputXml;
	}
	
	public void setPrintInputXml() throws SystemException, ApplicationException {
		this.printInputXml = servicesUtil.getWorksheetServiceXmlNonStatic(dynaFieldMap, creditApp, fsOid, financeSourceId, new Locale(Locale.ENGLISH.getLanguage(), Locale.CANADA.getCountry()), dlrOid, user);
	}

	public void setServicesUtil(ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}
	
	public void setManagerFactory(ManagerFactory managerFactory) {
		this.managerFactory = managerFactory;
	}

	public void setWorksheetContractTermsDAUtil(WorksheetContractTermsDAUtil worksheetContractTermsDAUtil) {
		this.worksheetContractTermsDAUtil = worksheetContractTermsDAUtil;
	}

	public void setRequestManager(RequestManager requestManager) {
		this.requestManager = requestManager;
	}

	public void setEsignPackageTotalDocumentSize() throws SystemException {
		dForm.set(WorksheetConstants.TOTAL_DOCUMENT_SIZE, String.valueOf(worksheetUtil.getUnprocessedVersionDocSizeNonStatic(caOid, request)));   
	}
	
	public void setUserTimeZone() {
		if(user != null && user.getTimeZone() == null){
			user.setTimeZone(Constants.GMT_TIMEZONE);
		}
	}
	
	public void saveFinalInterestRateToWorksheet() {
		if(WorksheetConstants.YES_VALUE.equals((String)dForm.getMap().get(WorksheetConstants.ContractTermsConstants.CPP)) && finalIntRate !=null && StringUtils.isNotEmpty(finalIntRate)){
           worksheetUtil.setDynaFieldNonStatic(dForm.getMap(), WorksheetConstants.Keys.DYNA_MAP_KEY, WorksheetConstants.ContractProgram.FINAL_INTEREST_RATE, TOFieldTransformations.doubleToString(new Double(finalIntRate), WorksheetConstants.TWO_DECIMAL_PLACES, context.getLocale()));
		}
	}
	
	public void populateDefaultWorksheetAndCreditAppFields() throws SystemException {
		worksheetUtil.setCLMDefaultFieldsNonStatic(dForm, caForm, financeSourceId, dealerState);
        worksheetUtil.updateContractFormNumberNonStatic(dForm, caForm, financeSourceId);
        worksheetContractTermsDAUtil.setFieldsFromDONonStatic(dynaFieldMap, worksheetContractTermsDAUtil.getWorksheetContractTermsNonStatic(null, (String) caForm.get(WorksheetConstants.CreditApp.Fields.TRANS_TYPE), dForm.getMap(), user, context.getLocale(), TimeZone.getTimeZone(worksheetUtil.getDealerTimeZone(request))), Locale.US);
	}
	
	public void saveeSignDocuments() throws SystemException, ApplicationException {
		if(CollectionUtils.isEmpty(documentContextIdList)){
        	Long oid = TOFieldTransformations.stringToLong((String) dForm.get(WorksheetConstants.ContractContextConstants.OID));
        	managerFactory.createWorksheetManager().updateeSignPrintGenerationNumber(dynaFieldMap, oid);
        	requestManager.saveEsignDocuments(request, dForm, creditApp, printInputXml, documentContextIdList);
        }	
	}
	
	public void setWorksheetTaxAndFeeDescriptions() throws SystemException {
		ServicesUtil.populateDescritions(request, dynaFieldMap,  new Locale(printLang, context.getLocale().getCountry()));
	}
	
	public void copyFsDetailsFromCreditAppToWorksheet() throws SystemException {
		worksheetUtil.setDescriptorNameInFormNonStatic(dForm, caForm);
	}
	
	public void setClientIdValues() {
		dynaFieldMap.put(Constants.CLIENTID_SECTION_ESIGNFLOW, WorksheetConstants.YES_VALUE);
	}
	
}