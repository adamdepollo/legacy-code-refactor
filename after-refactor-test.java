package com.refactor.worksheet.manager;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.validator.DynaValidatorActionForm;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.natpryce.makeiteasy.MakeItEasy;
import com.refactor.UseJFigFileForTest;
import com.refactor.common.Constants;
import com.refactor.common.i18n.I18NContext;
import com.refactor.common.services.ServicesUtil;
import com.refactor.common.util.CommonUtil;
import com.refactor.creditapplication.action.WorksheetAction;
import com.refactor.creditapplication.dataobject.CreditApplicationDO;
import com.refactor.creditapplication.makers.CreditApplicationDOMaker;
import com.refactor.creditapplication.makers.CreditApplicationDocMapMaker;
import com.refactor.creditapplication.util.TOUtils;
import com.refactor.creditapplication.util.WorksheetUtil;
import com.refactor.domain.User;
import com.refactor.esign.managers.DisplayManager;
import com.refactor.esign.managers.RequestManager;
import com.refactor.framework.exception.newex.ApplicationException;
import com.refactor.framework.exception.newex.SystemException;
import com.refactor.worksheet.base.AbstractManagerTest;
import com.refactor.worksheet.constant.WorksheetConstants;
import com.refactor.worksheet.domain.WorksheetContractTerms;
import com.refactor.worksheet.doutil.WorksheetContractTermsDAUtil;
import com.refactor.worksheet.form.WSDynaValidatorActionForm;
import com.refactor.worksheet.to.PrintDocument;
import com.refactor.worksheet.to.PrintDocumentsTO;

@RunWith(MockitoJUnitRunner.class)
public class RefactoredManagerTest extends AbstractManagerTest {
	@ClassRule
	public static UseJFigFileForTest useJFigFileForTest = new UseJFigFileForTest("test.config.xml");
	
	@Mock
	private ActionMapping mockActionMapping;
	@Mock
	private ActionForward mockActionForward;
	@Mock
	private ActionForm mockActionForm;
	@Mock
	private DynaValidatorActionForm mockdForm;
	@Mock
	private HttpServletRequest mockHttpServletRequest;
	@Mock
	private HttpServletResponse mockHttpServletResponse;
	@Mock
	private HttpSession mockHttpSession;
	@Mock
	private DynaValidatorActionForm mockCreditAppForm;
	@Mock
	private User mockedUser;
	@Mock
	private CreditApplicationDO mockedCreditAppDO;
	@Mock
	private ActionServlet mockActionServlet;
	@Mock
	private WorksheetUtil mockWorksheetUtil;
	@Mock
	private CommonUtil mockCommonUtil;
	@Mock
	private I18NContext mockContext;
	@Mock
	private WorksheetContractTerms mockWorksheetContractTerms;
	@Mock
	private DisplayManager mockDisplayManager;
	@Mock
	private WorksheetContractTermsDAUtil mockWorksheetContractTermsDAUtil;
	@Mock
	private WSDynaValidatorActionForm mockWSDynaValidatorActionForm;
	@Mock
	private WarrantyAndMaintenanceServiceManager mockWarrantyAndMaintenanceServiceManager;
	@Mock
	private ServicesUtil mockServicesUtil;
	@Mock
	private RequestManager mockRequestManager;
	@Mock
	private PrintDocumentsTO mockPrintDocumenstTO;
	@Mock
	private WorksheetAction mockWorksheetAction;
	
	private RefactoredManager managerSubject;
	
	private Map finalInterestRate;
	
	private Map dynaFields;
	
	private I18NContext ctx;
	
	@Before
	public void initialize() throws Exception{
		MockitoAnnotations.initMocks(this);
		finalInterestRate = new HashMap<>();
		dynaFields = new HashMap<>();
		ctx = new I18NContext();
		Locale locale = Locale.CANADA;
		ctx.setLocale(locale);
		mockWarrantyAndMaintenanceServiceManager = managerFactoryWithMocks.createWarrantyAndMaintenanceServiceManager();
		mockWorksheetUtil = managerFactoryWithMocks.createWorksheetUtil();
		managerSubject = new RefactoredManager();
		managerSubject.setDisplayManager(mockDisplayManager);
		managerSubject.setWorksheetUtil(mockWorksheetUtil);
		managerSubject.setWorksheetContractTermsDAUtil(mockWorksheetContractTermsDAUtil);
		managerSubject.setServicesUtil(mockServicesUtil);
		managerSubject.setRequestManager(mockRequestManager);
		managerSubject.setManagerFactory(managerFactoryWithMocks);
	}
	
	@Test
	public void saveEsignDocumentsWithNoDocumentContextIdsTest() throws SystemException, ApplicationException {
		dynaFields.put(WorksheetConstants.ContractContextConstants.FNC_SRC_OID, "12345");
		when(managerFactoryWithMocks.createWorksheetWrapper().getCreditApplicationDocMap(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(null);
		managerSubject.setCreditApp(MakeItEasy.make(CreditApplicationDOMaker.createCreditAppDO));
		managerSubject.setDynaFieldMap(dynaFields);
		managerSubject.setRequest(mockHttpServletRequest);
		managerSubject.setPrintInputXml("string");
		managerSubject.setdForm(mockdForm);
		managerSubject.setDocumentContextIdList();
		final RefactoredManager mockRefactoredManager = Mockito.spy(this.managerSubject);
		mockRefactoredManager.saveeSignDocuments();
		verify(mockRequestManager).saveEsignDocuments(Mockito.any(HttpServletRequest.class), Mockito.any(DynaValidatorActionForm.class), Mockito.any(CreditApplicationDO.class), anyString(), Mockito.anySetOf(UUID.class));
	}
	
	@Test
	public void saveEsignDocumentsWithDocumentContextIdsTest() throws SystemException, ApplicationException {
		final Set<UUID> contextIds = new HashSet<>();
		contextIds.add(new UUID(0, 0));
		dynaFields.put(WorksheetConstants.ContractContextConstants.FNC_SRC_OID, "12345");
		managerSubject.setCreditApp(MakeItEasy.make(CreditApplicationDOMaker.createCreditAppDO));
		managerSubject.setDynaFieldMap(dynaFields);
		managerSubject.setRequest(mockHttpServletRequest);
		managerSubject.setPrintInputXml("string");
		managerSubject.setdForm(mockdForm);
		managerSubject.setDocumentContextIdList(contextIds);
		final RefactoredManager mockRefactoredManager = Mockito.spy(this.managerSubject);
		mockRefactoredManager.saveeSignDocuments();
		verify(mockRequestManager, Mockito.never()).saveEsignDocuments(Mockito.any(HttpServletRequest.class), Mockito.any(DynaValidatorActionForm.class), Mockito.any(CreditApplicationDO.class), anyString(), Mockito.anySetOf(UUID.class));
	}
	
	@Test
    public void initializeDataValuesTest() throws SystemException, ApplicationException {
		final RefactoredManager manager = Mockito.spy(this.managerSubject);
		finalInterestRate.put(WorksheetConstants.ContractProgram.FINAL_INTEREST_RATE, ".05");
		dynaFields.put(WorksheetConstants.ContractContextConstants.FNC_SRC_OID, "12345");
		when(mockHttpServletRequest.getSession(false)).thenReturn(mockHttpSession);
		when(mockHttpSession.getAttribute("creditAppDynaValidatorActionForm")).thenReturn(mockCreditAppForm);
		when(mockWorksheetUtil.getCreditApplication(Mockito.any(HttpServletRequest.class))).thenReturn(mockCreditAppForm);
		when(mockWorksheetUtil.getDealerStateNonStatic(mockHttpServletRequest)).thenReturn("MI");
		when(mockdForm.getMap().get(WorksheetConstants.ContractProgram.FINAL_INTEREST_RATE)).thenReturn(finalInterestRate);
		when(mockHttpSession.getAttribute(CASConstants.INTERNATIONALIZATION_CONTEXT)).thenReturn(ctx);
		when(mockdForm.get(WorksheetConstants.ContractContextConstants.PRINT_LANGUAGE)).thenReturn("English");
		when(mockCreditAppForm.get(TOUtils.CREDIT_APPLICATION_SELECTED_FS)).thenReturn(new String[] { "1" });
		when(mockHttpSession.getAttribute(CASConstants.AUDIT_USER)).thenReturn(mockedUser);
		when(mockCreditAppForm.get("creditAppOid")).thenReturn("01234555");
		when(mockDisplayManager.getCreditApplicationDO(anyString())).thenReturn(MakeItEasy.make(CreditApplicationDOMaker.createCreditAppDO));
		when(mockHttpSession.getAttribute(CASConstants.ROUTEONE_DEALER_OID)).thenReturn(123456L);
		when(mockdForm.get(WorksheetConstants.ContractContextConstants.FNC_SRC_ID)).thenReturn("12345");
		when(mockWorksheetUtil.copyMapAndDynaFieldsNonStatic(mockdForm)).thenReturn(dynaFields);
		when(mockHttpSession.getAttribute(CASConstants.ROUTEONE_DEALER_TIME_ZONE)).thenReturn("GMT");
		when(mockCreditAppForm.get(WorksheetConstants.CreditApp.Fields.TRANS_TYPE)).thenReturn("transtype");
		when(mockWorksheetContractTermsDAUtil.getWorksheetContractTermsNonStatic(Mockito.any(WorksheetContractTerms.class), anyString(), Mockito.any(Map.class), Mockito.any(User.class), Mockito.any(Locale.class), Mockito.any(TimeZone.class))).thenReturn(mockWorksheetContractTerms);
		when(mockServicesUtil.getWorksheetServiceXmlNonStatic(Mockito.any(Map.class), Mockito.any(CreditApplicationDO.class), anyString(), anyString(), Mockito.any(Locale.class), anyLong(), Mockito.any(User.class))).thenReturn("xml");
        when(managerFactoryWithMocks.createWorksheetUtil().getWorkSheetPrintableDocuments(mockHttpServletRequest, mockActionForm)).thenReturn(createPrintDocumentsTO());
        when(mockActionMapping.findForward("displayeSignPage")).thenReturn(mockActionForward);
        when(mockdForm.get(WorksheetConstants.ContractContextConstants.OID)).thenReturn("1234567");
        when(managerFactoryWithMocks.createWorksheetWrapper().getCreditApplicationDocMap(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(CreditApplicationDocMapMaker.aListWith(CreditApplicationDocMapMaker.CREDIT_APP_DOC_MAP_WITH_CONTRACT, CreditApplicationDocMapMaker.CREDIT_APP_DOC_MAP_WITH_CREDITAPP));
        manager.initializeDataValues(mockdForm, mockHttpServletRequest, mockCASWorksheetAction);
        assertNotNull(manager.getCaForm());
        assertNotNull(manager.getCaOid());
        assertNotNull(manager.getContext());
        assertNotNull(manager.getCreditApp());
        assertNotNull(manager.getDealerState());
        assertNotNull(manager.getdForm());
        assertNotNull(manager.getDlrOid());
        assertNotNull(manager.getDynaFieldMap());
        assertNotNull(manager.getFinalIntRate());
        assertNotNull(manager.getFinanceSourceId());
        assertNotNull(manager.getFsOid());
        assertNotNull(manager.getPrintInputXml());
        assertNotNull(manager.getPrintLang());
        assertNotNull(manager.getSession());
        assertNotNull(manager.getUser());
        verify(manager).saveFinalInterestRateToWorksheet();
        verify(manager).setWorksheetTaxAndFeeDescriptions();
        verify(manager).copyFsDetailsFromCreditAppToWorksheet();
        verify(manager).populateDefaultWorksheetAndCreditAppFields();
        verify(manager).setEsignPackageTotalDocumentSize();
        verify(manager).setUserTimeZone();
	}
	
	private PrintDocumentsTO createPrintDocumentsTO(){
		List<PrintDocument> list = new ArrayList<>();
		PrintDocument document = new PrintDocument("contract", "NA_contract", 1, "2", "2", "contract", 2, null);
		PrintDocument document1 = new PrintDocument("creditApp", "NA_CreditApp", 1, "2", "2", "creditApp", 2, null);
		list.add(document);
		list.add(document1);
		PrintDocumentsTO documentsTO = new PrintDocumentsTO(list, "docPackageKey", "docPackageVersion");

		return documentsTO;
	}
}