public final ActionForward saveEsignDocumentsAndViewSignStatus(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws CASSystemException, CASApplicationException {
        DynaValidatorActionForm caform = WorksheetUtil.getCreditApp(request);
        DynaValidatorActionForm dform = (DynaValidatorActionForm) form;
        String dealerState = WorksheetUtil.getDealerState(request);
        String finalIntRate = (String)dform.getMap().get(WorksheetConstants.ContractProgram.FINAL_INTEREST_RATE);
        WorksheetUtil.setDescriptorNameInForm(dform, caform);
        I18NContext context = this.getI18NContext(request.getSession(false));
        String printLang = (String) dform.get(WorksheetConstants.ContractContextConstants.PRINT_LANGUAGE);
        String caOid = String.valueOf(caform.get(WorksheetConstants.CreditApp.Fields.OID));      
        dform.set(WorksheetConstants.TOTAL_DOCUMENT_SIZE, String.valueOf(WorksheetUtil.getUnprocessedVersionDocSize(caOid, request)));
        HttpSession session = request.getSession(false);
        CreditApplicationDO creditApp = getCreditApp(caform);
        Long dlrOid = (Long) session.getAttribute(CASConstants.ROUTEONE_DEALER_OID);
        User user = CommonUtil.getAuditUser(request);
        if(user != null && user.getTimeZone() == null){
               user.setTimeZone(CASConstants.GMT_TIMEZONE);
        }
        if(WorksheetConstants.YES_VALUE.equals((String)dform.getMap().get(WorksheetConstants.ContractTermsConstants.WSC_CPP)) && finalIntRate!=null && StringUtils.isNotEmpty(finalIntRate)){
               WorksheetUtil.setDynaField(dform.getMap(), WorksheetConstants.Keys.DYNA_MAP_KEY, WorksheetConstants.ContractProgram.FINAL_INTEREST_RATE, TOFieldTransformations.doubleToString(new Double(finalIntRate), WorksheetConstants.TWO_DECIMAL_PLACES, context.getLocale()));
        }
        String financeSourceId = (String) dform.get(WorksheetConstants.ContractContextConstants.FNC_SRC_ID);
        
        //set the Default values for Form number, Revision date
        WorksheetUtil.setCLMDefaultFields(dform, caform, financeSourceId,dealerState);
        Map fields = WorksheetUtil.copyMapAndDynaFields(dform);
        String fsOid = (String) fields.get(WorksheetConstants.ContractContextConstants.FNC_SRC_OID);
        WorksheetContractTermsDAUtil.setFieldsFromDO(fields, WorksheetContractTermsDAUtil.getWorksheetContractTerms(null, (String)caform.get(WorksheetConstants.CreditApp.Fields.TRANS_TYPE),dform.getMap(), user, context.getLocale(), TimeZone.getTimeZone(WorksheetUtil.getDealerTimeZone(request))), Locale.US);
        setSelectedWarrantyPlanDescription((WSDynaValidatorActionForm) form, user);
        ServicesUtil.populateDescritions(request,fields,  new Locale(printLang, context.getLocale().getCountry()));
        fields.put(CASConstants.CLIENTID_SECTION_ESIGNFLOW, WorksheetConstants.YES_VALUE);
        Set<UUID> documentContextIdList = wsceSignRequestManager.getDocumentContextIds(wscManagerFactory.createWorksheetWrapper().getCreditApplicationDocMap(creditApp.getCrdtAplcnOid(), wsceSignRequestManager.getDocumentTypeArray()));
        if(CollectionUtils.isEmpty(documentContextIdList)){
        	Long wscOid = TOFieldTransformations.stringToLong((String) dform.get(WorksheetConstants.ContractContextConstants.WSC_OID));
        	String printInputXml = ServicesUtil.getWorksheetServiceXml(fields, creditApp, fsOid, financeSourceId, new Locale(Locale.ENGLISH.getLanguage(), Locale.CANADA.getCountry()), dlrOid, user);
        	wscManagerFactory.createWorksheetManager().updateeSignPrintGenerationNumber(fields, wscOid);
        	wsceSignRequestManager.saveEsignDocuments(request, dform, creditApp, printInputXml, documentContextIdList);
        }