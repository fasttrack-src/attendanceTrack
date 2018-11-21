package test.java.api;

import java.lang.reflect.Field;
import java.security.Key;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import main.java.api.AttendanceService;
import main.java.api.SaveAttendanceRequest;
import main.java.api.DeleteAttendanceRequest;

public class AttendanceServiceTest {
	private AttendanceService service;
	private SaveAttendanceRequest request = new SaveAttendanceRequest();
	private DeleteAttendanceRequest deleteRequest = new DeleteAttendanceRequest();
	private Cookie cookie;
	
	
	@Before
	public void setUp() {
		service = new AttendanceService();
		request.setBarcode("123456789");
		request.setYear(2017);
		request.setMonth(1);
		request.setDate(1);
		request.setHour(1);
		request.setMinute(1);
		request.setGuid("test");
		
		deleteRequest.setBarcode("123456789");
		deleteRequest.setYear(2017);
		deleteRequest.setMonth(1);
		deleteRequest.setDate(1);
		deleteRequest.setGuid("test");
		
		//Get private key via reflection to create valid token
		try {
			Field keyField = AttendanceService.class.getDeclaredField("key");
			keyField.setAccessible(true);
			String token = Jwts.builder()
					  .setSubject(request.getGuid())
					  .signWith(SignatureAlgorithm.HS512, (Key) keyField.get(service))
					  .compact();
			cookie = new Cookie("guid", token);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSubmitBarcodeInvalidBarcode() {
		request.setBarcode("a423840912");
		
		Response r = service.submitAttendance(request, cookie);
		assert(r.getStatus() == 400);
	}
	
	@Test
	public void testSubmitBarcodeInvalidYear() {
		request.setYear(-1);
		Response r = service.submitAttendance(request, cookie);
		assert(r.getStatus() == 400);
	}
	
	@Test
	public void testSubmitBarcodeInvalidMonth() {
		request.setMonth(0);
		Response r = service.submitAttendance(request, cookie);
		assert(r.getStatus() == 400);
		request.setMonth(32);
		r = service.submitAttendance(request, cookie);
		assert(r.getStatus() == 400);
	}
	
	@Test
	public void testSubmitBarcodeInvalidDate() {
		request.setDate(0);
		Response r = service.submitAttendance(request, cookie);
		assert(r.getStatus() == 400);
		request.setDate(32);
		r = service.submitAttendance(request, cookie);
		assert(r.getStatus() == 400);
	}

	@Test
	public void testSubmitBarcodeInvalidHour() {
		request.setHour(-1);
		Response r = service.submitAttendance(request, cookie);
		assert(r.getStatus() == 400);
		request.setHour(32);
		r = service.submitAttendance(request, cookie);
		assert(r.getStatus() == 400);
	}
	
	@Test
	public void testSubmitBarcodeInvalidMinute() {
		request.setMinute(-1);
		Response r = service.submitAttendance(request, cookie);
		assert(r.getStatus() == 400);
		request.setMinute(97);
		r = service.submitAttendance(request, cookie);
		assert(r.getStatus() == 400);
	}
	
	@Test
	public void testDeleteBarcodeInvalidBarcode() {
		deleteRequest.setBarcode("a423840912");
		Response r = service.deleteAttendance(deleteRequest, cookie);
		assert(r.getStatus() == 400);
	}
	
	@Test
	public void testDeleteBarcodeInvalidYear() {
		deleteRequest.setYear(-1);
		Response r = service.deleteAttendance(deleteRequest, cookie);
		assert(r.getStatus() == 400);
	}
	
	@Test
	public void testDeleteBarcodeInvalidMonth() {
		deleteRequest.setMonth(0);
		Response r = service.deleteAttendance(deleteRequest, cookie);
		assert(r.getStatus() == 400);
		deleteRequest.setMonth(32);
		r = service.deleteAttendance(deleteRequest, cookie);
		assert(r.getStatus() == 400);
	}
	
	@Test
	public void testDeleteBarcodeInvalidDate() {
		deleteRequest.setDate(0);
		Response r = service.deleteAttendance(deleteRequest, cookie);
		assert(r.getStatus() == 400);
		deleteRequest.setDate(32);
		r = service.deleteAttendance(deleteRequest, cookie);
		assert(r.getStatus() == 400);
	}
}