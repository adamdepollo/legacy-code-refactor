package com.routeone.cas.worksheet.manager;

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

import com.routeone.cas.common.CASConstants;
import com.routeone.cas.common.i18n.I18NContext;
import com.routeone.cas.common.services.ServicesUtil;
import com.routeone.cas.common.util.CommonUtil;
import com.routeone.cas.creditapplication.action.CASCreditAppDispatchAction;
import com.routeone.cas.creditapplication.dataobject.CreditApplicationDO;
import com.routeone.cas.creditapplication.util.TOFieldTransformations;
import com.routeone.cas.creditapplication.util.WorksheetUtil;
import com.routeone.cas.domain.User;
import com.routeone.cas.esign.managers.WSCeSignDisplayManager;
import com.routeone.cas.esign.managers.WSCeSignRequestManager;
import com.routeone.cas.framework.exception.newex.CASApplicationException;
import com.routeone.cas.framework.exception.newex.CASSystemException;
import com.routeone.cas.worksheet.base.AbstractManager;
import com.routeone.cas.worksheet.constant.WorksheetConstants;
import com.routeone.cas.worksheet.doutil.WorksheetContractTermsDAUtil;
import com.routeone.cas.worksheet.factory.WscManagerFactory;

/**
 * @author Adam DePollo 5/14/2020
 *
 */
public class SaveEsignDocumentsAndViewSignStatusManager extends AbstractManager{
	
	private WSCeSignDisplayManager displayManager = new WSCeSignDisplayManager();
	private WorksheetUtil worksheetUtil = new WorksheetUtil();
	private ServicesUtil servicesUtil = new ServicesUtil();
	private WorksheetContractTermsDAUtil worksheetContractTermsDAUtil = new WorksheetContractTermsDAUtil();
	private WSCeSignRequestManager wsceSignRequestManager = new WSCeSignRequestManager();
	private WscManagerFactory wscManagerFactory = new WscManagerFactory();
	
	private DynaValidatorActionForm caForm;
	private DynaValidatorActionForm dForm;
	private HttpServletRequest request;
	private String dealerState;
	private String finalIntRate;
	private CASCreditAppDispatchAction subject;
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
	
	public void initializeDataValues(DynaValidatorActionForm dForm, HttpServletRequest request, CASCreditAppDispatchAction subject) throws CASSystemException, CASApplicationException {
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

	public CASCreditAppDispatchAction getSubject() {
		return subject;
	}

	public void setSubject(CASCreditAppDispatchAction subject) {
		this.subject = subject;
	}

	public I18NContext getContext() {
		return context;
	}

	public void setContext() {
		this.context = (I18NContext) request.getSession(false).getAttribute(CASConstants.INTERNATIONALIZATION_CONTEXT);
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

	public WSCeSignDisplayManager getDisplayManager() {
		return displayManager;
	}

	public void setDisplayManager(WSCeSignDisplayManager displayManager) {
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
		this.dlrOid = (Long) session.getAttribute(CASConstants.ROUTEONE_DEALER_OID);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public void setUser() throws CASSystemException {
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
	
	public void setDocumentContextIdList() throws CASSystemException {
		this.documentContextIdList = wsceSignRequestManager.getDocumentContextIds(wscManagerFactory.createWorksheetWrapper().getCreditApplicationDocMap(creditApp.getCrdtAplcnOid(), wsceSignRequestManager.getDocumentTypeArray()));
        
	}

	public String getPrintInputXml() {
		return printInputXml;
	}
	
	public void setPrintInputXml(String printInputXml) {
		this.printInputXml = printInputXml;
	}
	
	public void setPrintInputXml() throws CASSystemException, CASApplicationException {
		this.printInputXml = servicesUtil.getWorksheetServiceXmlNonStatic(dynaFieldMap, creditApp, fsOid, financeSourceId, new Locale(Locale.ENGLISH.getLanguage(), Locale.CANADA.getCountry()), dlrOid, user);
	}

	public void setServicesUtil(ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}
	
	public void setWscManagerFactory(WscManagerFactory wscManagerFactory) {
		this.wscManagerFactory = wscManagerFactory;
	}

	public void setWorksheetContractTermsDAUtil(WorksheetContractTermsDAUtil worksheetContractTermsDAUtil) {
		this.worksheetContractTermsDAUtil = worksheetContractTermsDAUtil;
	}

	public void setWsceSignRequestManager(WSCeSignRequestManager wsceSignRequestManager) {
		this.wsceSignRequestManager = wsceSignRequestManager;
	}

	public void setEsignPackageTotalDocumentSize() throws CASSystemException {
		dForm.set(WorksheetConstants.TOTAL_DOCUMENT_SIZE, String.valueOf(worksheetUtil.getUnprocessedVersionDocSizeNonStatic(caOid, request)));   
	}
	
	public void setUserTimeZone() {
		if(user != null && user.getTimeZone() == null){
			user.setTimeZone(CASConstants.GMT_TIMEZONE);
		}
	}
	
	public void saveFinalInterestRateToWorksheet() {
		if(WorksheetConstants.YES_VALUE.equals((String)dForm.getMap().get(WorksheetConstants.ContractTermsConstants.WSC_CPP)) && finalIntRate !=null && StringUtils.isNotEmpty(finalIntRate)){
           worksheetUtil.setDynaFieldNonStatic(dForm.getMap(), WorksheetConstants.Keys.DYNA_MAP_KEY, WorksheetConstants.ContractProgram.FINAL_INTEREST_RATE, TOFieldTransformations.doubleToString(new Double(finalIntRate), WorksheetConstants.TWO_DECIMAL_PLACES, context.getLocale()));
		}
	}
	
	public void populateDefaultWorksheetAndCreditAppFields() throws CASSystemException {
		worksheetUtil.setCLMDefaultFieldsNonStatic(dForm, caForm, financeSourceId, dealerState);
        worksheetUtil.updateContractFormNumberNonStatic(dForm, caForm, financeSourceId);
        worksheetContractTermsDAUtil.setFieldsFromDONonStatic(dynaFieldMap, worksheetContractTermsDAUtil.getWorksheetContractTermsNonStatic(null, (String) caForm.get(WorksheetConstants.CreditApp.Fields.TRANS_TYPE), dForm.getMap(), user, context.getLocale(), TimeZone.getTimeZone(worksheetUtil.getDealerTimeZone(request))), Locale.US);
	}
	
	public void saveeSignDocuments() throws CASSystemException, CASApplicationException {
		if(CollectionUtils.isEmpty(documentContextIdList)){
        	Long wscOid = TOFieldTransformations.stringToLong((String) dForm.get(WorksheetConstants.ContractContextConstants.WSC_OID));
        	wscManagerFactory.createWorksheetManager().updateeSignPrintGenerationNumber(dynaFieldMap, wscOid);
        	wsceSignRequestManager.saveEsignDocuments(request, dForm, creditApp, printInputXml, documentContextIdList);
        }	
	}
	
	public void setWorksheetTaxAndFeeDescriptions() throws CASSystemException {
		ServicesUtil.populateDescritions(request, dynaFieldMap,  new Locale(printLang, context.getLocale().getCountry()));
	}
	
	public void copyFsDetailsFromCreditAppToWorksheet() throws CASSystemException {
		worksheetUtil.setDescriptorNameInFormNonStatic(dForm, caForm);
	}
	
	public void setClientIdValues() {
		dynaFieldMap.put(CASConstants.CLIENTID_SECTION_ESIGNFLOW, WorksheetConstants.YES_VALUE);
	}
	
}