package main.java.api;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.crypto.MacProvider;
import main.java.api.db.DbConnection;
import main.java.api.db.DeleteAttendanceQuery;
import main.java.api.db.GetAllGroupsQuery;
import main.java.api.db.GetGroupsPerTutorQuery;
import main.java.api.db.GetStudentsInGroupQuery;
import main.java.api.db.GetTutorIdFromLoginQuery;
import main.java.api.db.NumberOfGroupsPerIdQuery;
import main.java.api.db.NumberOfStudentsPerIdQuery;
import main.java.api.db.StudentAttendanceHistoryQuery;
import main.java.api.db.StudentAttendanceTodayQuery;
import main.java.api.db.StudentIdPerBarcodeQuery;
import main.java.api.db.StudentTakesCourseQuery;
import main.java.api.db.SubmitAttendanceQuery;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * REST endpoint, used to receive attendance data from Ionic application
 * 
 * @author Maria
 *
 */
@Path("/")
public class AttendanceService {
	private static Logger LOGGER = LogManager.getLogger("AttendanceService");
	private static Key key = MacProvider.generateKey();
	
	@Context
	ServletContext context;
	
	/**
	 * Creates a new cookie that will expire in 24 hours and returns it in response
	 * @param guid - GUID of user who is logging in
	 * @return HTTP 200 OK response containing new cookie
	 */
	private Response createCookie(String guid, boolean isTutor) {
		String token = Jwts.builder()
				  .setSubject(guid)
				  .setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000 * 24))
				  .signWith(SignatureAlgorithm.HS512, key)
				  .compact();
		NewCookie cookie = new NewCookie("token", token);
		LoginResponse response = new LoginResponse(isTutor);
				
		return Response.ok(response, MediaType.APPLICATION_JSON).cookie(cookie).build();
	}
	
	/**
	 * Checks whether GUID belongs to a tutor and the GUID and password are valid
	 * @param request - Contains GUID and password
	 * @return Response with status code 200 OK
	 */
	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(LoginRequest request) {
		Connection c = null;
		LDAPConnection ldapC = null;
		try {
			//Establish DB connection
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			ConfigManager config = mapper.readValue(new File(context.getRealPath("WEB-INF/classes/config.yaml")),
					ConfigManager.class);
			DbConnection con = new DbConnection(config.getDatabaseUrl(), config.getDatabaseUser(),
					config.getDatabasePassword());
			c = con.getConnection();
			
			if(request.getGuid().equals("admin")) {
				if(new Password().checkPassword(request.getPassword(), config.getAdminPswdHash())) {
					//Create and return a cookie for the user's session
					return createCookie(request.getGuid(), true);
				}
				else {
					LOGGER.error("Attendance service received an invalid GUID or password. GUID: " + request.getGuid());
					return Response.status(400).entity(new ErrorResponse("Invalid username or password")).build();
				}
			}
			else {
				// Perform sequence of queries as a transaction
				c.setAutoCommit(false);
				
				//Check that GUID corresponds to a tutor
				String login = request.getGuid();
				GetTutorIdFromLoginQuery tutorIdQuery = new GetTutorIdFromLoginQuery(c);
				List<Integer> tutorIds = tutorIdQuery.getTutorId(login);
				if(tutorIds.size() > 0) {
					c.commit();
					return checkCredentials(config, request, true);
				}else {
					//Login doesn't correspond to a tutor
					//Check whether it corresponds to a student
					NumberOfStudentsPerIdQuery numStudentsQuery =  new NumberOfStudentsPerIdQuery(c);
					int numStudents = numStudentsQuery.getNumStudents(login);
					c.commit();
					if(numStudents <= 0) {
						LOGGER.error("Attendance service received login not registered to a student or tutor. Login: " + login);
						return Response.status(400).entity(new ErrorResponse("Invalid username or password")).build();
					}else if(numStudents > 1) {
						LOGGER.error("Attendance service received student login registered to more than one student. Login: " + login);
						return Response.status(400).entity(new ErrorResponse("Invalid username or password")).build();
					}else {
						return checkCredentials(config, request, false);
					}
				}
			}
		} catch (ClassNotFoundException e) {
			LOGGER.error("Attendance service couldn't connect to the database", e);
			return Response.status(500).entity(new ErrorResponse("Couldn't connect to the database")).build();
		} catch (JsonParseException | JsonMappingException e) {
			LOGGER.error("Attendance service couldn't parse config file", e);
			return Response.status(500).entity(new ErrorResponse("Error in backend service")).build();
		} catch (IOException e) {
			LOGGER.error("Attendance service couldn't open config file", e);
			return Response.status(500).entity(new ErrorResponse("Error in backend service")).build();
		} catch (SQLException e) {
			LOGGER.error("Attendance service encountered an error while working in the database", e);
			return Response.status(500).entity(new ErrorResponse("Error in database")).build();
		} catch (LDAPException e) {
			if(e.getResultCode().intValue() == 49) {
				LOGGER.error("Attendance service received an invalid GUID or password. GUID: " + request.getGuid());
				return Response.status(400).entity(new ErrorResponse("Invalid GUID or password")).build();
			}else {
				LOGGER.error("Attendance service could not connect to LDAP server", e);
				return Response.status(500).entity(new ErrorResponse("Could not connect to LDAP server")).build();	
			}
		} finally {
			try {
				if(c != null) {
					c.close();
				}
				if(ldapC != null) {
					ldapC.close();	
				}
			} catch (SQLException e) {
				LOGGER.error("Attendance service couldn't close connection to the database", e);
			}
		}
	}
	
	/**
	 * Checks username and password
	 * @param config - ConfigManager needed to get LDAP URL
	 * @param request - Contains username and password
	 * @param isTutor - Marks whether the login belongs to a tutor or student
	 * @return HTTP 200 Response on success
	 * @throws LDAPException
	 */
	private Response checkCredentials(ConfigManager config, LoginRequest request, boolean isTutor) throws LDAPException {
		//Check that GUID and password are correct
		String host = config.getLdapUrl();
		int port = config.getLdapPort();
		String username = request.getGuid() + "@DCS";
		String password = request.getPassword();
		LDAPConnection ldapC = new LDAPConnection(host, port, username, password);

		//Create and return a cookie for the user's session
		return createCookie(request.getGuid(), isTutor);
	}

	/**
	 * Logs out the user by passing a new cookie and removing the old one in the browser
	 * @param cookie
	 * @return A successful response with no cookie
	 */
	@GET
	@Path("/logout")
	@Produces(MediaType.APPLICATION_JSON)
	public Response logout(@CookieParam("token") Cookie cookie) {
		LOGGER.info("Attendance service received a logout request");
		if (cookie != null) {
	        NewCookie newCookie = new NewCookie(cookie, null, 0, false);
	        return Response.ok().cookie(newCookie).build();
	    }
	    return Response.ok().build();
	}
	
	/**
	 * checks whether the token provided has a valid GUID
	 * @param token - token to be validated
	 * @param guid - ID that should be contained in token
	 * @return true if token is valid, false otherwise
	 * 
	 */
	private boolean isTokenValid(String token, String guid) {
		try {
			Jwts.parser().requireSubject(guid).setSigningKey(key).parseClaimsJws(token);
			return true;
		} catch (SignatureException | io.jsonwebtoken.ExpiredJwtException e) {
		    return false;
		}
	}
	
	/***
	 * @param request
	 *            - contains tutor ID
	 * @return if successful a list of tutorial groups for tutor
	 */
	@POST
	@Path("/getGroups")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGroupsPerTutor(GetGroupsRequest request, @CookieParam("token") Cookie cookie) {
		Connection c;
		//check if request has cookie with the same user id
		if(cookie == null || !isTokenValid(cookie.getValue(), request.getGuid())) {
			LOGGER.error("Attendance service received a request with missing or incorrect cookie");
			return Response.status(403).entity(new ErrorResponse("Please log in to see this page")).build();
		}else {
			try {
				c = getDbConnection();
			} catch (ClassNotFoundException | SQLException e) {
				LOGGER.error("Attendance service couldn't connect to the database", e);
				return Response.status(500).entity(new ErrorResponse("Couldn't connect to the database")).build();
			} catch (JsonParseException | JsonMappingException e) {
				LOGGER.error("Attendance service couldn't parse config file", e);
				return Response.status(500).entity(new ErrorResponse("Error in backend service")).build();
			} catch (IOException e) {
				LOGGER.error("Attendance service couldn't open config file", e);
				return Response.status(500).entity(new ErrorResponse("Error in backend service")).build();
			}

			try {
				// Perform sequence of queries as a transaction
				c.setAutoCommit(false);
				String login = request.getGuid();
				
				if(!login.equals("admin")) {
					//Check that login corresponds to a single tutor
					GetTutorIdFromLoginQuery tutorIdQuery = new GetTutorIdFromLoginQuery(c);
					List<Integer> tutorIds = tutorIdQuery.getTutorId(login);
					if(tutorIds.size() <= 0) {
						LOGGER.error("Attendance service received login not registered to a tutor. Login: " + login);
						return Response.status(400).entity(new ErrorResponse("Login not registered to a tutor")).build();
					}else {
						int tutorId = tutorIds.get(0);
						GetGroupsPerTutorQuery query = new GetGroupsPerTutorQuery(c);
						List<TutorialGroup> result = query.getGroupsPerTutor(tutorId);
						c.commit();

						return Response.ok(result, MediaType.APPLICATION_JSON).build();	
					}
				}else {
					return getAllGroups();
				}
			} catch (SQLException e) {
				LOGGER.error("Attendance service encountered an error while working in the database", e);
				return Response.status(500).entity(new ErrorResponse("Error in database")).build();
			} finally {
				try {
					c.close();
				} catch (SQLException e) {
					LOGGER.error("Attendance service couldn't close connection to the database", e);
				}
			}	
		}
	}
	
	/**
	 * 
	 * @return - if successful, all active tutorial groups
	 */
	@POST
	@Path("/getAllGroups")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGroups(GetGroupsRequest request, @CookieParam("token") Cookie cookie) {
		//check if request has cookie with the same user id
		if(cookie == null  || !isTokenValid(cookie.getValue(), request.getGuid())) {
			LOGGER.error("Attendance service received a request with missing or incorrect cookie");
			return Response.status(403).entity(new ErrorResponse("Please log in to see this page")).build();
		}else {
			return getAllGroups();
		}
	}

	private Response getAllGroups() {
		Connection c;
		try {
			c = getDbConnection();
		} catch (ClassNotFoundException | SQLException e) {
			LOGGER.error("Attendance service couldn't connect to the database", e);
			return Response.status(500).entity(new ErrorResponse("Couldn't connect to the database")).build();
		} catch (JsonParseException | JsonMappingException e) {
			LOGGER.error("Attendance service couldn't parse config file", e);
			return Response.status(500).entity(new ErrorResponse("Error in backend service")).build();
		} catch (IOException e) {
			LOGGER.error("Attendance service couldn't open config file", e);
			return Response.status(500).entity(new ErrorResponse("Error in backend service")).build();
		}
		try {
			GetAllGroupsQuery query = new GetAllGroupsQuery(c);
			List<TutorialGroup> result = query.getGroups();
			return Response.ok(result, MediaType.APPLICATION_JSON).build();	
		} catch (SQLException e) {
			LOGGER.error("Attendance service encountered an error while working in the database", e);
			return Response.status(500).entity(new ErrorResponse("Error in database")).build();
		} finally {
			try {
				c.close();
			} catch (SQLException e) {
				LOGGER.error("Attendance service couldn't close connection to the database", e);
			}
		}	
	}
	/**
	 * @param request
	 *            - contains ID of tutorial group
	 * @return students in specified tutorial group
	 */
	@POST
	@Path("/getStudentsInGroup")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStudentsInGroup(GetStudentsInGroupRequest request, @CookieParam("token") Cookie cookie) {
		//check if request has cookie with the same user id
		if(cookie == null || !isTokenValid(cookie.getValue(), request.getGuid())) {
			LOGGER.error("Attendance service received a request with missing or incorrect cookie");
			return Response.status(403).entity(new ErrorResponse("Please log in to see this page")).build();
		}else {
			Connection c;
			try {
				c = getDbConnection();
			} catch (ClassNotFoundException | SQLException e) {
				LOGGER.error("Attendance service couldn't connect to the database", e);
				return Response.status(500).entity(new ErrorResponse("Couldn't connect to the database")).build();
			} catch (JsonParseException | JsonMappingException e) {
				LOGGER.error("Attendance service couldn't parse config file", e);
				return Response.status(500).entity(new ErrorResponse("Error in backend service")).build();
			} catch (IOException e) {
				LOGGER.error("Attendance service couldn't open config file", e);
				return Response.status(500).entity(new ErrorResponse("Error in backend service")).build();
			}
	
			// Connection is established
			try {
				//Execute queries as a transaction
				c.setAutoCommit(false);
				int groupId = request.getGroupId();
				
				//Check that exactly one tutorial group corresponds to this ID
				NumberOfGroupsPerIdQuery numGroupsQuery = new NumberOfGroupsPerIdQuery(c);
				int numGroups = numGroupsQuery.getNumGroups(groupId);
				if(numGroups <= 0) {
					LOGGER.error("Attendance service received tutorial group ID not registered to a tutorial group. ID: " + groupId);
					return Response.status(400)
							.entity(new ErrorResponse("No tutorial group registered with this ID")).build();
				}else if(numGroups > 1) {
					LOGGER.error("Attendance service received tutorial group ID registered to more than one tutorial group. ID: " + groupId);
					return Response.status(400)
							.entity(new ErrorResponse("More than one tutorial group registered with this ID")).build();
				}else {
					GetStudentsInGroupQuery query = new GetStudentsInGroupQuery(c);
					List<Student> result = query.getStudentsInGroup(request, c);
					c.commit();
					return Response.ok(result, MediaType.APPLICATION_JSON).build();	
				}
			} catch (SQLException e) {
				LOGGER.error("Attendance service encountered an error while working in the database", e);
				return Response.status(500).entity(new ErrorResponse("Error in database")).build();
			} finally {
				try {
					c.close();
				} catch (SQLException e) {
					LOGGER.error("Attendance service couldn't close connection to the database", e);
				}
			}
		}
	}
	
	/**
	 * Returns the student's attendance history for the current academic year
	 * @param request - Contains student login
	 * @param cookie - Used for authentication
	 * @return HTTP 200 Response with student attendance history on success
	 */
	@POST
	@Path("/getHistory")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAttendanceHistory(GetStudentHistoryRequest request, @CookieParam("token") Cookie cookie) {
		//check if request has cookie with the same user id
		if(cookie == null || !isTokenValid(cookie.getValue(), request.getGuid())) {
			LOGGER.error("Attendance service received a request with missing or incorrect cookie");
			return Response.status(403).entity(new ErrorResponse("Please log in to see this page")).build();
		}else {
			Connection c;
			try {
				c = getDbConnection();
			} catch (ClassNotFoundException | SQLException e) {
				LOGGER.error("Attendance service couldn't connect to the database", e);
				return Response.status(500).entity(new ErrorResponse("Couldn't connect to the database")).build();
			} catch (JsonParseException | JsonMappingException e) {
				LOGGER.error("Attendance service couldn't parse config file", e);
				return Response.status(500).entity(new ErrorResponse("Error in backend service")).build();
			} catch (IOException e) {
				LOGGER.error("Attendance service couldn't open config file", e);
				return Response.status(500).entity(new ErrorResponse("Error in backend service")).build();
			}
	
			// Connection is established
			try {
				//Execute queries as a transaction
				c.setAutoCommit(false);
				String login = request.getStudentLogin();
				//Check that exactly one student corresponds to this login
				NumberOfStudentsPerIdQuery numStudentsQuery = new NumberOfStudentsPerIdQuery(c);
				int numStudents = numStudentsQuery.getNumStudents(login);
				if(numStudents <= 0) {
					c.commit();
					LOGGER.error("Attendance service received student login not registered to a student. Login: " + login);
					return Response.status(400).entity(new ErrorResponse("No student registered with this login")).build();
				}else if(numStudents > 1) {
					c.commit();
					LOGGER.error("Attendance service received student login registered to more than one student. Login: " + login);
					return Response.status(400)
							.entity(new ErrorResponse("More than one student registered with this login")).build();
				}else {
					StudentAttendanceHistoryQuery query = new StudentAttendanceHistoryQuery(c);
					List<AttendanceItem> result = query.getAttendanceHistory(login);
					c.commit();
					return Response.ok(result, MediaType.APPLICATION_JSON).build();	
				}
			} catch (SQLException e) {
				LOGGER.error("Attendance service encountered an error while working in the database", e);
				return Response.status(500).entity(new ErrorResponse("Error in database")).build();
			} finally {
				try {
					c.close();
				} catch (SQLException e) {
					LOGGER.error("Attendance service couldn't close connection to the database", e);
				}
			}
		}
	}

	/**
	 * Receives barcode, connects to database and stores it
	 * 
	 * @param request
	 *            - Contains barcode, date, and course information
	 * @return On success - HTTP Response 200 OK
	 */
	@POST
	@Path("/submitAttendance")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response submitAttendance(SaveAttendanceRequest request, @CookieParam("token") Cookie cookie) {
		//check if request has cookie with the same user id
		if(cookie == null || !isTokenValid(cookie.getValue(), request.getGuid())) {
			LOGGER.error("Attendance service received a request with missing or incorrect cookie");
			return Response.status(403).entity(new ErrorResponse("Please log in to see this page")).build();
		}else {
			String barcode = request.getBarcode();
			LOGGER.info("Attendance service received a barcode: " + barcode);
			// expect requests to contain a valid date and barcodes which consist only of
			// digits
			if (request.isValid()) {
				Connection c;
				try {
					c = getDbConnection();
				} catch (ClassNotFoundException | SQLException e) {
					LOGGER.error("Attendance service couldn't connect to the database", e);
					return Response.status(500).entity(new ErrorResponse("Couldn't connect to the database")).build();
				} catch (JsonParseException | JsonMappingException e) {
					LOGGER.error("Attendance service couldn't parse config file", e);
					return Response.status(500).entity(new ErrorResponse("Error in backend service")).build();
				} catch (IOException e) {
					LOGGER.error("Attendance service couldn't open config file", e);
					return Response.status(500).entity(new ErrorResponse("Error in backend service")).build();
				}
				try {
					// Perform sequence of queries as a transaction
					c.setAutoCommit(false);
	
					// Check that barcode corresponds to a single student
					StudentIdPerBarcodeQuery studentIdQuery = new StudentIdPerBarcodeQuery(c);
					List<Student> students = studentIdQuery.getStudentPerBarcode(barcode);
	
					if (students.size() <= 0) {
						LOGGER.error(String.format(
								"Attendance service received a barcode not registered to a student. Barcode: %s", barcode));
						return Response.status(400)
								.entity(new ErrorResponse("No student registered for this student card")).build();
	
					} else if (students.size() > 1) {
						LOGGER.error(String.format(
								"Attendance service received a barcode registered to more than one student. Barcode: %s ",
								barcode));
						return Response.status(400)
								.entity(new ErrorResponse("More than one student registered for this student card"))
								.build();
	
					} else {
						// Check that student takes course
						Student student = students.get(0);
						int studentId = student.getId();
						StudentTakesCourseQuery takesCourseQuery = new StudentTakesCourseQuery(c);
						if (!takesCourseQuery.doesStudentTakeCourse(studentId, request.getCourseId())) {
							LOGGER.error(String.format(
									"Attendance service received a barcode for student not enrolled in the course. "
											+ "Barcode: %s Student ID: %d Course ID: %d ",
									barcode, studentId, request.getCourseId()));
							return Response.status(400)
									.entity(new ErrorResponse("Student is not enrolled in this course")).build();
	
						} else {
							//Check that this hasn't already been recorded
							StudentAttendanceTodayQuery attendedQuery = new StudentAttendanceTodayQuery(c);
							if(attendedQuery.didStudentAttendToday(student.getLogin(), request.getCourse())) {
								LOGGER.info(String.format("Attendance service received request to save already existing attendance."
										+ "Barcode: %s, Course: %s, Date: %2d/%2d/%4d", 
										request.getBarcode(), 
										request.getCourse(),
										request.getDate(),
										request.getMonth(),
										request.getYear()));
								return Response.status(400)
										.entity(new ErrorResponse(
												"Attendance already saved for this student and course for today"))
										.build();
							}else {
								SubmitAttendanceQuery query = new SubmitAttendanceQuery(c);
								query.executeQuery(request);
								c.commit();
								// return HTTP response 200 in case of success
								return Response.ok(MediaType.APPLICATION_JSON).build();
							}
						}
					}
				} catch (SQLException e) {
					LOGGER.error("Attendance service encountered an error while working in the database", e);
					return Response.status(500).entity(new ErrorResponse("Error in database")).build();
				} finally {
					try {
						c.close();
					} catch (SQLException e) {
						LOGGER.error("Attendance service couldn't close connection to the database", e);
					}
				}
			}
			return Response.status(400).entity(new ErrorResponse("Barcode should be 12 digits")).build();
		}
	}

	@POST
	@Path("/deleteAttendance")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteAttendance(DeleteAttendanceRequest request, @CookieParam("token") Cookie cookie) {
		//check if request has cookie with the same user id
		if(cookie == null || !isTokenValid(cookie.getValue(), request.getGuid())) {
			LOGGER.error("Attendance service received a request with missing or incorrect cookie");
			return Response.status(403).entity(new ErrorResponse("Please log in to see this page")).build();
		}else {
			LOGGER.info("Attendance service received a request to delete attendance for barcode: " + request.getBarcode());
			// expect requests to contain a valid date and barcodes which consist only of
			// digits
			if (request.isValid()) {
				Connection c;
				try {
					c = getDbConnection();
				} catch (ClassNotFoundException | SQLException e) {
					LOGGER.error("Attendance service couldn't connect to the database", e);
					return Response.status(500).entity(new ErrorResponse("Couldn't connect to the database")).build();
				} catch (JsonParseException | JsonMappingException e) {
					LOGGER.error("Attendance service couldn't parse config file", e);
					return Response.status(500).entity(new ErrorResponse("Error in backend service")).build();
				} catch (IOException e) {
					LOGGER.error("Attendance service couldn't open config file", e);
					return Response.status(500).entity(new ErrorResponse("Error in backend service")).build();
				}
	
				try {
					DeleteAttendanceQuery query = new DeleteAttendanceQuery(c);
					query.executeQuery(request);
					// return HTTP response 200 in case of success
					return Response.ok(MediaType.APPLICATION_JSON).build();
				} catch (SQLException e) {
					LOGGER.error("Attendance service encountered an error while working in the database", e);
					return Response.status(500).entity(new ErrorResponse("Error in database")).build();
				} finally {
					try {
						c.close();
					} catch (SQLException e) {
						LOGGER.error("Attendance service couldn't close connection to the database", e);
					}
				}
			}
			return Response.status(400).entity(new ErrorResponse("Barcode should be 12 digits")).build();
		}
	}

	/**
	 * Used to verify that service is running
	 * 
	 * @return HTTP Response 200 OK
	 */
	@GET
	@Path("/verify")
	@Produces(MediaType.TEXT_PLAIN)
	public Response verifyService() {
		LOGGER.info("Service started");
		String result = "Service Successfully started..";
		// return HTTP response 200 in case of success
		return Response.status(200).entity(result).build();
	}
	
	/**
	 * @return a page detailing available AttendanceService endpoints
	 */
	@GET
	@Path("/")
	@Produces(MediaType.TEXT_HTML)
	public Response help() {
		String helpPage = "<title>Help Page</title>"
				+ "<div>Attendance service provides the following endpoints:</div>"
				+ "<ul>" + 
				"  <li>" + 
				"    login - Used to log tutor in to the application</br>" + 
				"    </br> Expects a JSON POST request of the following format: " + 
				"    </br> {\"guid\" : \"{Glasgow University ID}\", \"password\" : \"{SOCS password}\"}" + 
				"  </li>" + 
				"  </br>" + 
				"  <li>logout - Used to log tutor out of the application if they are logged in</li>" + 
				"  </br>" + 
				"  <li>" + 
				"    getGroups - Used to return a list of lab groups for a particular tutor </br></br>" + 
				"    Expects a JSON POST request of the following format: </br>" + 
				"    {\"guid\": \"{Glasgow University ID}\"}" + 
				"  </li>" + 
				"  </br>" + 
				"  <li>" + 
				"    getAllGroups - Used to return a list of all currently active lab groups regardless of who their tutor is</br></br>" + 
				"    Expects a JSON POST request of the following format: </br>" + 
				"    {\"guid\": \"{Glasgow University ID}\"}" + 
				"  </li>" + 
				"  </br>" + 
				"  <li>" + 
				"    getStudentsInGroup - Used to return a list of students in a lab group</br></br>" + 
				"    Expects a JSON POST request of the following format: </br>" + 
				"    {\"guid\": \"{Glasgow University ID}\", \"groupId\" : \"{Tutorial group ID}\", \"course\" : \"{Course name}\"}" + 
				"  </li>" + 
				"  </br>" + 
				"  <li>" + 
				"    submitAttendance - Used to submit items of attendance</br></br>" + 
				"    Expects a JSON POST request of the following format: </br>" + 
				"    {\"guid\": \"{Glasgow University ID}\", \"course\" : \"{Course name}\", \"courseId\" : \"{Course ID}\", \"barcode\" : \"{Student card barcode}\", \"year\" : \"{Year}\", \"month\" : \"{Month}\", \"date\" : \"{Day of month}\", \"hour\" : \"{Hour}\", \"minute\" : \"{minute}\"}" + 
				"  </li>" + 
				"  </br>" + 
				"  <li>" + 
				"    deleteAttendance - Used to delete an item of attendance (in case it has been submitted in error)</br></br>" + 
				"    Expects a JSON POST request of the following format: </br>" + 
				"    {\"guid\": \"{Glasgow University ID}\", \"course\" : \"{Course name}\", \"courseId\" : \"{Course ID}\", \"barcode\" : \"{Student card barcode}\", \"year\" : \"{Year}\", \"month\" : \"{Month}\", \"date\" : \"{Day of month}\"}" + 
				"  </li>" + 
				"</ul>";
		return Response.status(200).entity(helpPage).build();
	}

	private Connection getDbConnection()
			throws JsonParseException, JsonMappingException, IOException, ClassNotFoundException, SQLException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		ConfigManager config = mapper.readValue(new File(context.getRealPath("WEB-INF/classes/config.yaml")),
				ConfigManager.class);
		DbConnection con = new DbConnection(config.getDatabaseUrl(), config.getDatabaseUser(),
				config.getDatabasePassword());
		return con.getConnection();
	}
}
