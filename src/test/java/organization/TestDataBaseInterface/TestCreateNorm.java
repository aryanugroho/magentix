package organization.TestDataBaseInterface;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.junit.*;

import organization.TestDataBaseInterface.DatabaseAccess;
import es.upv.dsic.gti_ia.norms.BeliefDataBaseInterface;
import es.upv.dsic.gti_ia.norms.Norm;
import es.upv.dsic.gti_ia.organization.DataBaseInterface;
import es.upv.dsic.gti_ia.organization.exception.MySQLException;


/** 
 * @author Jose Alemany Bordera  -  jalemany1@dsic.upv.es
 * 
 */

public class TestCreateNorm {

	private DataBaseInterface dbI = null;
	private DatabaseAccess dbA = null;
	private BeliefDataBaseInterface bdbi = null;
	
	private Method m = null;
	
	
	@Before
	public void setUp() throws Exception {
		
		Class[] parameterTypes = new Class[4];
	    parameterTypes[0] = java.lang.String.class;
	    parameterTypes[1] = java.lang.String.class;
	    parameterTypes[2] = es.upv.dsic.gti_ia.norms.Norm.class;
	    parameterTypes[3] = java.lang.String.class;
		
	    m = DataBaseInterface.class.getDeclaredMethod("createNorm", parameterTypes);
		m.setAccessible(true);
		
		bdbi = new BeliefDataBaseInterface();
		dbA = new DatabaseAccess();

		//-------------  Clean Data Base  ------------//
		dbA.executeSQL("DELETE FROM agentPlayList");
		dbA.executeSQL("DELETE FROM agentList");
		dbA.executeSQL("DELETE FROM actionNormParam");
		dbA.executeSQL("DELETE FROM normList");
		dbA.executeSQL("DELETE FROM roleList WHERE idroleList != 1");
		dbA.executeSQL("DELETE FROM unitHierarchy WHERE idChildUnit != 1");
		dbA.executeSQL("DELETE FROM unitList WHERE idunitList != 1");

		//--------------------------------------------//
	}

	@After
	public void tearDown() throws Exception {

		//-------------  Clean Data Base  ------------//
		dbA.executeSQL("DELETE FROM agentPlayList");
		dbA.executeSQL("DELETE FROM agentList");
		dbA.executeSQL("DELETE FROM actionNormParam");
		dbA.executeSQL("DELETE FROM normList");
		dbA.executeSQL("DELETE FROM roleList WHERE idroleList != 1");
		dbA.executeSQL("DELETE FROM unitHierarchy WHERE idChildUnit != 1");
		dbA.executeSQL("DELETE FROM unitList WHERE idunitList != 1");

		//--------------------------------------------//

		dbA = null;
		dbI = null;
		bdbi = null;
		
		m = null;
	}
	
	@Test(timeout = 5 * 1000)
	public void testCreateNorm1() {
		
		/**---------------------------------------------------------------------------------
		 * --			1.	
		 * --				- All parameters are correct
		 * --				- TargetValue == '_'
		 * --				- Any agent
		 * --				- Norm will be created
		 * ---------------------------------------------------------------------------------
		 */
		
		try {	
			
			//------------------------------------------- Test Initialization  -----------------------------------------------//
			//Test variables
			String unit = "proofUnitFlat";
			String content = "proofContentNorm";
			String normName = "proofNorm";
			ArrayList<String> actions = new ArrayList<String>();
				actions.add("registerUnit");
				actions.add("registerUnit");
				actions.add("exampleUnitFlat");
				actions.add("flat");
				actions.add("virtual");
				actions.add("exampleAgent");
				actions.add("exampleRoleCreator2");
			String eRoleCreator = "exampleRoleCreator";
			String agentName = "exampleAgent";
			Norm norm = new Norm(normName, "f", "agentName", "_", actions, "", "");
			String rule = bdbi.buildNormRule(norm);
			int count1, count2, count3;
			//Data Base 
			dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('"+ unit +"',(SELECT idunitType FROM unitType WHERE unitTypeName = 'flat'))");
			dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleCreator +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
			
			dbA.executeSQL("INSERT INTO `agentList` (`agentName`) VALUES ('"+ agentName +"')");
			//----------------------------------------------------------------------------------------------------------------//
			
			dbI = new DataBaseInterface();
			
			Object[] parameters = new Object[4];
			parameters[0] = unit;
		    parameters[1] = content;
		    parameters[2] = norm;
		    parameters[3] = rule;
		    
			String result = (String) m.invoke(dbI, parameters);
			
			count1 = dbA.countQuery("SELECT * FROM normList");
		    count2 = dbA.countQuery("SELECT * FROM actionNormParam");
		    count3 = dbA.countQuery("SELECT * FROM agentList");
		    
			assertTrue(result.equals(normName+ " created") && count1==1 && count2==5 && count3==1);

		} catch(InvocationTargetException e) {
			
			fail(e.getTargetException().getMessage());
			
		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}	
	
	@Test(timeout = 5 * 1000)
	public void testCreateNorm2() {
		
		/**---------------------------------------------------------------------------------
		 * --			2.	
		 * --				- All parameters are correct
		 * --				- TargetValue == '_'
		 * --				- Any role
		 * --				- Norm will be created
		 * ---------------------------------------------------------------------------------
		 */
		
		try {	
			
			//------------------------------------------- Test Initialization  -----------------------------------------------//
			//Test variables
			String unit = "proofUnitFlat";
			String content = "proofContentNorm";
			String normName = "proofNorm";
			ArrayList<String> actions = new ArrayList<String>();
				actions.add("registerUnit");
				actions.add("registerUnit");
				actions.add("exampleUnitFlat");
				actions.add("flat");
				actions.add("virtual");
				actions.add("exampleAgent");
				actions.add("exampleRoleCreator2");
			String eRoleCreator = "exampleRoleCreator";
			String eRoleMember = "exampleRoleMember";
			Norm norm = new Norm(normName, "f", "roleName", "_", actions, "", "");
			String rule = bdbi.buildNormRule(norm);
			int count1, count2, count3;
			//Data Base 
			dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('"+ unit +"',(SELECT idunitType FROM unitType WHERE unitTypeName = 'flat'))");
			dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleCreator +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleMember +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'member'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'internal'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'private'))");
			//----------------------------------------------------------------------------------------------------------------//
			
			dbI = new DataBaseInterface();
			
			Object[] parameters = new Object[4];
			parameters[0] = unit;
		    parameters[1] = content;
		    parameters[2] = norm;
		    parameters[3] = rule;
		    
			String result = (String) m.invoke(dbI, parameters);
			
			count1 = dbA.countQuery("SELECT * FROM normList");
		    count2 = dbA.countQuery("SELECT * FROM actionNormParam");
		    count3 = dbA.countQuery("SELECT * FROM agentList");
		    
			assertTrue(result.equals(normName+ " created") && count1==1 && count2==5 && count3==0);

		} catch(InvocationTargetException e) {
			
			fail(e.getTargetException().getMessage());
			
		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}
	
	@Test(timeout = 5 * 1000)
	public void testCreateNorm3() {
		
		/**---------------------------------------------------------------------------------
		 * --			3.	
		 * --				- All parameters are correct
		 * --				- TargetValue == '_'
		 * --				- Any position
		 * --				- Norm will be created
		 * ---------------------------------------------------------------------------------
		 */
		
		try {	
			
			//------------------------------------------- Test Initialization  -----------------------------------------------//
			//Test variables
			String unit = "proofUnitFlat";
			String content = "proofContentNorm";
			String normName = "proofNorm";
			ArrayList<String> actions = new ArrayList<String>();
				actions.add("registerUnit");
				actions.add("registerUnit");
				actions.add("exampleUnitFlat");
				actions.add("flat");
				actions.add("virtual");
				actions.add("exampleAgent");
				actions.add("exampleRoleCreator2");
			String eRoleCreator = "exampleRoleCreator";
			String eRoleMember = "exampleRoleMember";
			Norm norm = new Norm(normName, "f", "positionName", "_", actions, "", "");
			String rule = bdbi.buildNormRule(norm);
			int count1, count2, count3;
			//Data Base 
			dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('"+ unit +"',(SELECT idunitType FROM unitType WHERE unitTypeName = 'flat'))");
			dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleCreator +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleMember +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'member'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'internal'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'private'))");
			//----------------------------------------------------------------------------------------------------------------//
			
			dbI = new DataBaseInterface();
			
			Object[] parameters = new Object[4];
			parameters[0] = unit;
		    parameters[1] = content;
		    parameters[2] = norm;
		    parameters[3] = rule;
		    
			String result = (String) m.invoke(dbI, parameters);
			
			count1 = dbA.countQuery("SELECT * FROM normList");
		    count2 = dbA.countQuery("SELECT * FROM actionNormParam");
		    count3 = dbA.countQuery("SELECT * FROM agentList");
		    
			assertTrue(result.equals(normName+ " created") && count1==1 && count2==5 && count3==0);

		} catch(InvocationTargetException e) {
			
			fail(e.getTargetException().getMessage());
			
		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}
	
	@Test(timeout = 5 * 1000)
	public void testCreateNorm4() {
		
		/**---------------------------------------------------------------------------------
		 * --			4.	
		 * --				- All parameters are correct
		 * --				- TargetValue == idagentList
		 * --				- ExampleAgent exists
		 * --				- Norm will be created
		 * ---------------------------------------------------------------------------------
		 */
		
		try {	
			
			//------------------------------------------- Test Initialization  -----------------------------------------------//
			//Test variables
			String unit = "proofUnitFlat";
			String content = "proofContentNorm";
			String normName = "proofNorm";
			ArrayList<String> actions = new ArrayList<String>();
				actions.add("registerUnit");
				actions.add("registerUnit");
				actions.add("exampleUnitFlat");
				actions.add("flat");
				actions.add("virtual");
				actions.add("exampleAgent");
				actions.add("exampleRoleCreator2");
			String eRoleCreator = "exampleRoleCreator";
			String agentName = "exampleAgent";
			Norm norm = new Norm(normName, "f", "agentName", agentName, actions, "", "");
			String rule = bdbi.buildNormRule(norm);
			int count1, count2, count3;
			//Data Base 
			dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('"+ unit +"',(SELECT idunitType FROM unitType WHERE unitTypeName = 'flat'))");
			dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleCreator +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
			
			dbA.executeSQL("INSERT INTO `agentList` (`agentName`) VALUES ('"+ agentName +"')");
			//----------------------------------------------------------------------------------------------------------------//
			
			dbI = new DataBaseInterface();
			
			Object[] parameters = new Object[4];
			parameters[0] = unit;
		    parameters[1] = content;
		    parameters[2] = norm;
		    parameters[3] = rule;
		    
		    String result = (String) m.invoke(dbI, parameters);
			
			count1 = dbA.countQuery("SELECT * FROM normList");
		    count2 = dbA.countQuery("SELECT * FROM actionNormParam");
		    count3 = dbA.countQuery("SELECT * FROM agentList");
		    
			assertTrue(result.equals(normName+ " created") && count1==1 && count2==5 && count3==1);

		} catch(InvocationTargetException e) {
			
			fail(e.getTargetException().getMessage());
			
		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}
	

	@Test(timeout = 5 * 1000)
	public void testCreateNorm5() {
		
		/**---------------------------------------------------------------------------------
		 * --			5.	
		 * --				- All parameters are correct
		 * --				- TargetValue == idagentList
		 * --				- ExampleAgent don't exists
		 * --				- Norm will be created
		 * ---------------------------------------------------------------------------------
		 */
		
		try {	
			
			//------------------------------------------- Test Initialization  -----------------------------------------------//
			//Test variables
			String unit = "proofUnitFlat";
			String content = "proofContentNorm";
			String normName = "proofNorm";
			ArrayList<String> actions = new ArrayList<String>();
				actions.add("registerUnit");
				actions.add("registerUnit");
				actions.add("exampleUnitFlat");
				actions.add("flat");
				actions.add("virtual");
				actions.add("exampleAgent");
				actions.add("exampleRoleCreator2");
			String eRoleCreator = "exampleRoleCreator";
			String agentName = "exampleAgent";
			Norm norm = new Norm(normName, "f", "agentName", agentName, actions, "", "");
			String rule = bdbi.buildNormRule(norm);
			int count1, count2, count3;
			//Data Base 
			dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('"+ unit +"',(SELECT idunitType FROM unitType WHERE unitTypeName = 'flat'))");
			dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleCreator +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
			//----------------------------------------------------------------------------------------------------------------//
			
			dbI = new DataBaseInterface();
			
			Object[] parameters = new Object[4];
			parameters[0] = unit;
		    parameters[1] = content;
		    parameters[2] = norm;
		    parameters[3] = rule;
		    
		    String result = (String) m.invoke(dbI, parameters);
			
			count1 = dbA.countQuery("SELECT * FROM normList");
		    count2 = dbA.countQuery("SELECT * FROM actionNormParam");
		    count3 = dbA.countQuery("SELECT * FROM agentList");
		    
			assertTrue(result.equals(normName+ " created") && count1==1 && count2==5 && count3==1);

		} catch(InvocationTargetException e) {
			
			fail(e.getTargetException().getMessage());
			
		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}
	
	@Test(timeout = 5 * 1000)
	public void testCreateNorm6() {
		
		/**---------------------------------------------------------------------------------
		 * --			6.	
		 * --				- All parameters are correct
		 * --				- TargetValue == idroleList
		 * --				- Norm will be created
		 * ---------------------------------------------------------------------------------
		 */
		
		try {	
			
			//------------------------------------------- Test Initialization  -----------------------------------------------//
			//Test variables
			String unit = "proofUnitFlat";
			String content = "proofContentNorm";
			String normName = "proofNorm";
			ArrayList<String> actions = new ArrayList<String>();
				actions.add("registerUnit");
				actions.add("registerUnit");
				actions.add("exampleUnitFlat");
				actions.add("flat");
				actions.add("virtual");
				actions.add("exampleAgent");
				actions.add("exampleRoleCreator2");
			String eRoleCreator = "exampleRoleCreator";
			String eRoleMember = "exampleRoleMember";
			Norm norm = new Norm(normName, "f", "roleName", eRoleMember, actions, "", "");
			String rule = bdbi.buildNormRule(norm);
			int count1, count2, count3;
			//Data Base 
			dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('"+ unit +"',(SELECT idunitType FROM unitType WHERE unitTypeName = 'flat'))");
			dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleCreator +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleMember +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'member'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'internal'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'private'))");
			//----------------------------------------------------------------------------------------------------------------//
			
			dbI = new DataBaseInterface();
			
			Object[] parameters = new Object[4];
			parameters[0] = unit;
		    parameters[1] = content;
		    parameters[2] = norm;
		    parameters[3] = rule;
		    
		    String result = (String) m.invoke(dbI, parameters);
			
			count1 = dbA.countQuery("SELECT * FROM normList");
		    count2 = dbA.countQuery("SELECT * FROM actionNormParam");
		    count3 = dbA.countQuery("SELECT * FROM agentList");
		    
			assertTrue(result.equals(normName+ " created") && count1==1 && count2==5 && count3==0);

		} catch(InvocationTargetException e) {
			
			fail(e.getTargetException().getMessage());
			
		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}
	
	@Test(timeout = 5 * 1000)
	public void testCreateNorm7() {
		
		/**---------------------------------------------------------------------------------
		 * --			7.	
		 * --				- All parameters are correct
		 * --				- TargetValue == idposition
		 * --				- Norm will be created
		 * ---------------------------------------------------------------------------------
		 */
		
		try {	
			
			//------------------------------------------- Test Initialization  -----------------------------------------------//
			//Test variables
			String unit = "proofUnitFlat";
			String content = "proofContentNorm";
			String normName = "proofNorm";
			ArrayList<String> actions = new ArrayList<String>();
				actions.add("registerUnit");
				actions.add("registerUnit");
				actions.add("exampleUnitFlat");
				actions.add("flat");
				actions.add("virtual");
				actions.add("exampleAgent");
				actions.add("exampleRoleCreator2");
			String eRoleCreator = "exampleRoleCreator";
			String eRoleMember = "exampleRoleMember";
			String pos = "member";
			Norm norm = new Norm(normName, "f", "positionName", pos, actions, "", "");
			String rule = bdbi.buildNormRule(norm);
			int count1, count2, count3;
			//Data Base 
			dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('"+ unit +"',(SELECT idunitType FROM unitType WHERE unitTypeName = 'flat'))");
			dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleCreator +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleMember +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'member'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'internal'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'private'))");
			//----------------------------------------------------------------------------------------------------------------//
			
			dbI = new DataBaseInterface();
			
			Object[] parameters = new Object[4];
			parameters[0] = unit;
		    parameters[1] = content;
		    parameters[2] = norm;
		    parameters[3] = rule;
		    
		    String result = (String) m.invoke(dbI, parameters);
			
			count1 = dbA.countQuery("SELECT * FROM normList");
		    count2 = dbA.countQuery("SELECT * FROM actionNormParam");
		    count3 = dbA.countQuery("SELECT * FROM agentList");
		    
			assertTrue(result.equals(normName+ " created") && count1==1 && count2==5 && count3==0);

		} catch(InvocationTargetException e) {
			
			fail(e.getTargetException().getMessage());
			
		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}
	
	@Test(timeout = 5 * 1000)
	public void testCreateNorm8() {
		
		/**---------------------------------------------------------------------------------
		 * --			8.	
		 * --				- Any parameters are incorrect
		 * --				- Unit and role don't exist
		 * --				- Norm won't be created
		 * ---------------------------------------------------------------------------------
		 */
		
		try {	
			
			//------------------------------------------- Test Initialization  -----------------------------------------------//
			//Test variables
			String content = "proofContentNorm";
			String normName = "proofNorm";
			ArrayList<String> actions = new ArrayList<String>();
				actions.add("registerUnit");
				actions.add("registerUnit");
				actions.add("exampleUnitFlat");
				actions.add("flat");
				actions.add("virtual");
				actions.add("exampleAgent");
				actions.add("exampleRoleCreator2");
			String eRoleMember = "NotExists";
			Norm norm = new Norm(normName, "f", "roleName", eRoleMember, actions, "", "");
			String rule = bdbi.buildNormRule(norm);
			//----------------------------------------------------------------------------------------------------------------//
			
			dbI = new DataBaseInterface();
			
			Object[] parameters = new Object[4];
			parameters[0] = "NotExists";
		    parameters[1] = content;
		    parameters[2] = norm;
		    parameters[3] = rule;
		    
			String result = (String) m.invoke(dbI, parameters);
			fail(result);

		} catch(InvocationTargetException e) {
			
			assertTrue(e.getTargetException().getMessage(), e.getTargetException() instanceof MySQLException);
			
		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}
	
	@Test(timeout = 5 * 1000)
	public void testCreateNorm9() {
		
		/**---------------------------------------------------------------------------------
		 * --			9.	
		 * --				- Any parameters are incorrect
		 * --				- Iddeontic field doesn't exist
		 * --				- Norm won't be created
		 * ---------------------------------------------------------------------------------
		 */
		
		try {	
			
			//------------------------------------------- Test Initialization  -----------------------------------------------//
			//Test variables
			String unit = "proofUnitFlat";
			String content = "proofContentNorm";
			String normName = "proofNorm";
			ArrayList<String> actions = new ArrayList<String>();
				actions.add("registerUnit");
				actions.add("registerUnit");
				actions.add("exampleUnitFlat");
				actions.add("flat");
				actions.add("virtual");
				actions.add("exampleAgent");
				actions.add("exampleRoleCreator2");
			String eRoleCreator = "exampleRoleCreator";
			String eRoleMember = "exampleRoleMember";
			Norm norm = new Norm(normName, "NotExists", "roleName", eRoleMember, actions, "", "");
			String rule = bdbi.buildNormRule(norm);
			//Data Base 
			dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('"+ unit +"',(SELECT idunitType FROM unitType WHERE unitTypeName = 'flat'))");
			dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleCreator +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleMember +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'member'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'internal'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'private'))");
			//----------------------------------------------------------------------------------------------------------------//
			
			dbI = new DataBaseInterface();
			
			Object[] parameters = new Object[4];
			parameters[0] = unit;
		    parameters[1] = content;
		    parameters[2] = norm;
		    parameters[3] = rule;
		    
			String result = (String) m.invoke(dbI, parameters);
			fail(result);

		} catch(InvocationTargetException e) {
			
			assertTrue(e.getTargetException().getMessage(), e.getTargetException() instanceof MySQLException);
			
		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}
	
	@Test(timeout = 5 * 1000)
	public void testCreateNorm10() {
		
		/**---------------------------------------------------------------------------------
		 * --			10.	
		 * --				- Any parameters are incorrect
		 * --				- TargetType field doesn't exist
		 * --				- Norm won't be created
		 * ---------------------------------------------------------------------------------
		 */
		
		try {	
			
			//------------------------------------------- Test Initialization  -----------------------------------------------//
			//Test variables
			String unit = "proofUnitFlat";
			String content = "proofContentNorm";
			String normName = "proofNorm";
			ArrayList<String> actions = new ArrayList<String>();
				actions.add("registerUnit");
				actions.add("registerUnit");
				actions.add("exampleUnitFlat");
				actions.add("flat");
				actions.add("virtual");
				actions.add("exampleAgent");
				actions.add("exampleRoleCreator2");
			String eRoleCreator = "exampleRoleCreator";
			String eRoleMember = "exampleRoleMember";
			Norm norm = new Norm(normName, "f", "NotExists", eRoleMember, actions, "", "");
			String rule = bdbi.buildNormRule(norm);
			//Data Base 
			dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('"+ unit +"',(SELECT idunitType FROM unitType WHERE unitTypeName = 'flat'))");
			dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleCreator +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleMember +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'member'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'internal'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'private'))");
			//----------------------------------------------------------------------------------------------------------------//
			
			dbI = new DataBaseInterface();
			
			Object[] parameters = new Object[4];
			parameters[0] = unit;
		    parameters[1] = content;
		    parameters[2] = norm;
		    parameters[3] = rule;
		    
			String result = (String) m.invoke(dbI, parameters);
			fail(result);

		} catch(InvocationTargetException e) {
			
			assertTrue(e.getTargetException().getMessage(), e.getTargetException() instanceof MySQLException);
			
		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}
	
	@Test(timeout = 5 * 1000)
	public void testCreateNorm11() {
		
		/**---------------------------------------------------------------------------------
		 * --			11.	
		 * --				- Any parameters are incorrect
		 * --				- Role doesn't exist
		 * --				- Norm won't be created
		 * ---------------------------------------------------------------------------------
		 */
		
		try {	
			
			//------------------------------------------- Test Initialization  -----------------------------------------------//
			//Test variables
			String unit = "proofUnitFlat";
			String content = "proofContentNorm";
			String normName = "proofNorm";
			ArrayList<String> actions = new ArrayList<String>();
				actions.add("registerUnit");
				actions.add("registerUnit");
				actions.add("exampleUnitFlat");
				actions.add("flat");
				actions.add("virtual");
				actions.add("exampleAgent");
				actions.add("exampleRoleCreator2");
			String eRoleCreator = "exampleRoleCreator";
			String eRoleMember = "NotExists";
			Norm norm = new Norm(normName, "f", "roleName", eRoleMember, actions, "", "");
			String rule = bdbi.buildNormRule(norm);
			//Data Base 
			dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('"+ unit +"',(SELECT idunitType FROM unitType WHERE unitTypeName = 'flat'))");
			dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleCreator +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
			//----------------------------------------------------------------------------------------------------------------//
			
			dbI = new DataBaseInterface();
			
			Object[] parameters = new Object[4];
			parameters[0] = unit;
		    parameters[1] = content;
		    parameters[2] = norm;
		    parameters[3] = rule;
		    
			String result = (String) m.invoke(dbI, parameters);
			fail(result);

		} catch(InvocationTargetException e) {
			
			assertTrue(e.getTargetException().getMessage(), e.getTargetException() instanceof MySQLException);
			
		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}
	
	@Test(timeout = 5 * 1000)
	public void testCreateNorm12() {
		
		/**---------------------------------------------------------------------------------
		 * --			12.	
		 * --				- Any parameters are incorrect
		 * --				- Position doesn't exist
		 * --				- Norm won't be created
		 * ---------------------------------------------------------------------------------
		 */
		
		try {	
			
			//------------------------------------------- Test Initialization  -----------------------------------------------//
			//Test variables
			String unit = "proofUnitFlat";
			String content = "proofContentNorm";
			String normName = "proofNorm";
			ArrayList<String> actions = new ArrayList<String>();
				actions.add("registerUnit");
				actions.add("registerUnit");
				actions.add("exampleUnitFlat");
				actions.add("flat");
				actions.add("virtual");
				actions.add("exampleAgent");
				actions.add("exampleRoleCreator2");
			String eRoleCreator = "exampleRoleCreator";
			String eRoleMember = "exampleRoleMember";
			String pos = "NotExists";
			Norm norm = new Norm(normName, "f", "positionName", pos, actions, "", "");
			String rule = bdbi.buildNormRule(norm);
			//Data Base 
			dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('"+ unit +"',(SELECT idunitType FROM unitType WHERE unitTypeName = 'flat'))");
			dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleCreator +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleMember +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'member'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'internal'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'private'))");
			//----------------------------------------------------------------------------------------------------------------//
			
			dbI = new DataBaseInterface();
			
			Object[] parameters = new Object[4];
			parameters[0] = unit;
		    parameters[1] = content;
		    parameters[2] = norm;
		    parameters[3] = rule;
		    
			String result = (String) m.invoke(dbI, parameters);
			fail(result);

		} catch(InvocationTargetException e) {
			
			assertTrue(e.getTargetException().getMessage(), e.getTargetException() instanceof MySQLException);
			
		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}
	
	@Test(timeout = 5 * 1000)
	public void testCreateNorm13() {
		
		/**---------------------------------------------------------------------------------
		 * --			13.	
		 * --				- Any parameters are incorrect
		 * --				- ActionName doesn't exist
		 * --				- Norm won't be created
		 * ---------------------------------------------------------------------------------
		 */
		
		try {	
			
			//------------------------------------------- Test Initialization  -----------------------------------------------//
			//Test variables
			String unit = "proofUnitFlat";
			String content = "proofContentNorm";
			String normName = "proofNorm";
			ArrayList<String> actions = new ArrayList<String>();
				actions.add("notExists");
				actions.add("notExists");
				actions.add("exampleUnitFlat");
				actions.add("flat");
				actions.add("virtual");
				actions.add("exampleAgent");
				actions.add("exampleRoleCreator2");
			String eRoleCreator = "exampleRoleCreator";
			String eRoleMember = "exampleRoleMember";
			Norm norm = new Norm(normName, "f", "roleName", eRoleMember, actions, "", "");
			String rule = bdbi.buildNormRule(norm);
			//Data Base 
			dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('"+ unit +"',(SELECT idunitType FROM unitType WHERE unitTypeName = 'flat'))");
			dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleCreator +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleMember +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'member'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'internal'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'private'))");
			//----------------------------------------------------------------------------------------------------------------//
			
			dbI = new DataBaseInterface();
			
			Object[] parameters = new Object[4];
			parameters[0] = unit;
		    parameters[1] = content;
		    parameters[2] = norm;
		    parameters[3] = rule;
		    
			String result = (String) m.invoke(dbI, parameters);
			fail(result);

		} catch(InvocationTargetException e) {
			
			assertTrue(e.getTargetException().getMessage(), e.getTargetException() instanceof MySQLException);
			
		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}
}
