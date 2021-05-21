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

	
	/* 
	...
	removed dozens of additional setters for the instance variables for readability
	*/
	
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