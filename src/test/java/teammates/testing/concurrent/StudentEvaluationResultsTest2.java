package teammates.testing.concurrent;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

import teammates.testing.lib.BrowserInstance;
import teammates.testing.lib.BrowserInstancePool;
import teammates.testing.lib.TMAPI;
import teammates.testing.object.Scenario;
import teammates.testing.object.Student;

public class StudentEvaluationResultsTest2 extends TestCase {

	static Scenario scn = setupScenarioInstance("scenario");
	static BrowserInstance bi;
	private final static int FIRST_EVALUATION = 0;

	@BeforeClass
	public static void classSetup() {
		bi = BrowserInstancePool.getBrowserInstance();
		TMAPI.cleanupCourse(scn.course.courseId);

		TMAPI.createCourse(scn.course);
		TMAPI.createEvaluation(scn.evaluation);
		TMAPI.enrollStudents(scn.course.courseId, scn.students);
		TMAPI.createEvaluation(scn.evaluation);
		TMAPI.studentsJoinCourse(scn.students, scn.course.courseId);
		TMAPI.openEvaluation(scn.course.courseId, scn.evaluation.name);
		TMAPI.studentsSubmitFeedbacks(scn.students, scn.course.courseId, scn.evaluation.name);
		TMAPI.closeEvaluation(scn.course.courseId, scn.evaluation.name);
		TMAPI.publishEvaluation(scn.course.courseId, scn.evaluation.name);
	}

	@AfterClass
	public static void classTearDown() {
		if (bi.isElementPresent(bi.logoutTab))
			bi.logout();
		TMAPI.cleanupCourse(scn.course.courseId);
		BrowserInstancePool.release(bi);
	}

	@Test
	public void testStudentViewEvaluationResultsSuccessful() throws Exception {
		for (Student student : scn.students) {
			studentViewEvaluationResults(student);
		}
	}

	private void studentViewEvaluationResults(Student student) {
		bi.studentLogin(student.email, student.password);

		bi.clickEvaluationTab();
		bi.justWait();

		bi.studentClickEvaluationViewResults(scn.course.courseId, scn.evaluation.name);
		bi.justWait();

		// comments order is random
		for (int i = 0; i < scn.students.size(); i++) {
			Student teammate = scn.students.get(i);
			if (teammate.teamName.equals(student.teamName) && !teammate.name.equals(student.name)) {
				bi.justWait();
				assertTrue(bi.studentGetFeedbackFromOthers(teammate.email, student.email));
			}
		}

		bi.clickEvaluationTab();
		bi.studentClickEditEvaluation(scn.course.courseId, scn.evaluation.name);
		
		assertTrue(bi.isElementPresent(bi.getStudentEvaluationViewResults(FIRST_EVALUATION)));
		assertTrue(bi.isElementPresent(bi.getStudentEditEvaluation(FIRST_EVALUATION)));

		bi.logout();
	}
}
