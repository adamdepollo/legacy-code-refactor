public final ActionForward saveEsignDocumentsAndViewSignStatus(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws CASSystemException, CASApplicationException {
        SaveEsignDocumentsAndViewSignStatusManager manager = wscManagerFactory.createSaveeSignDocumentsAndViewSignStatusManager();
		manager.initializeDataValues(castActionFormToDynaValidatorActionForm(form), request, this);
        setSelectedWarrantyPlanDescription(castActionFormToWSDynaValidatorActionForm(form), manager.getUser());
        manager.saveeSignDocuments();
        return mapping.findForward("displayeSignPage");
    }
	
	public DynaValidatorActionForm castActionFormToDynaValidatorActionForm(ActionForm form) {
		return (DynaValidatorActionForm) form;
	}
	
	public WSDynaValidatorActionForm castActionFormToWSDynaValidatorActionForm(ActionForm form) {
		return (WSDynaValidatorActionForm) form;
	}