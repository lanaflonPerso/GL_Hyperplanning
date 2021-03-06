package admin;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import global.MysqlConnection;
import com.vaadin.addon.calendar.event.BasicEvent;
import com.vaadin.addon.calendar.event.BasicEventProvider;
import com.vaadin.addon.calendar.ui.Calendar;
import com.vaadin.addon.calendar.ui.CalendarComponentEvents;
import com.vaadin.addon.calendar.ui.CalendarComponentEvents.EventClick;
import com.vaadin.addon.calendar.ui.CalendarComponentEvents.MoveEvent;
import com.vaadin.addon.calendar.ui.CalendarTargetDetails;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DateField;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.DragStartMode;
import com.vaadin.ui.DragAndDropWrapper.WrapperTransferable;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class AddPlanningCalendar extends CustomComponent {
	public HorizontalLayout hl = new HorizontalLayout();
	public HorizontalLayout hl2 = new HorizontalLayout();

	public VerticalLayout vl = new VerticalLayout();	
	public Select classSubjectInputField=new Select("Matiere:");	
	public TextField classTimeInputField=new TextField("Duree(minute):");
	public TextField classWeekInputField=new TextField("Nombre de semaine:");
	public Select classroomInputField=new Select("Salle:");
	public Select classTypeInputField=new Select("Type:");
	public Select classTeacherInputField=new Select("Professeur:");
	public Button moveMe=new Button("Déplacez le cours sur le créneau horaire souhaité");
	public Button confirmAddPlanning=new Button("Valider");
	public Button reset=new Button("Reset");
	private MysqlConnection con= new MysqlConnection();;

	public String nom;
	public String type;
	Window change = new Window("Modification");
	Calendar cal = new Calendar();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public AddPlanningCalendar(String nom, String type) throws Exception {
		vl.setSpacing(true);
		vl.setMargin(true);
		this.nom = nom;
		this.type = type;
		setCompositionRoot(hl);
		cal.setFirstVisibleDayOfWeek(1);
		cal.setLastVisibleDayOfWeek(6);
		cal.setFirstVisibleHourOfDay(8);
		cal.setLastVisibleHourOfDay(20);
		// cal.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
		cal.setHeight("450px");
		cal.setWidth("900px");
		BasicEventProvider eventProvider = (BasicEventProvider) cal
				.getEventProvider();
				
		
		if(type=="groupe") {									
			ResultSet rs = con.queryTable("select cours.ID_matiere,salles.nom sallenom,cours.Date_debut,cours.Date_Fin FROM cours,groupe,salles " +
					"Where cours.ID_groupe_cours=groupe.ID AND cours.id_salle=salles.id AND groupe.Nom='"
					+nom+"'");
			

			while (rs.next()) {
				Date date_debut = sdf.parse(rs.getString("Date_debut"));
				Date date_fin = sdf.parse(rs.getString("Date_Fin"));

				ResultSet rs2 =con.queryTable("select nom from matiere where id='"+rs.getString("id_matiere")+"'");
				while (rs2.next()) {
				eventProvider.addEvent(new BasicEvent(rs2.getString("nom"), "",
						date_debut, date_fin));
				}
			}
		;
			cal.setDropHandler(new DropHandler(){

				public void drop(DragAndDropEvent event) {
					CalendarTargetDetails details = (CalendarTargetDetails) event
							.getTargetDetails();
					WrapperTransferable transferable = (WrapperTransferable) event
							.getTransferable();
					try {
						createEventByGroup(details, transferable);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				public AcceptCriterion getAcceptCriterion() {
					return AcceptAll.get();
				}
			});
			cal.setHandler(new CalendarComponentEvents.EventClickHandler() {
				public void eventClick(EventClick event) {
					BasicEvent e = (BasicEvent) event.getCalendarEvent();
					try {
						changeWindow(e);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			cal.setHandler(new CalendarComponentEvents.EventMoveHandler() {

				public void eventMove(MoveEvent event) {

				BasicEvent e = (BasicEvent) event.getCalendarEvent();				
				String date_begin=sdf.format(e.getStart());
				String date_end=sdf.format(e.getEnd());
				long currentDateRange = e.getEnd().getTime()
						- e.getStart().getTime();
				String date_begin_new=sdf.format(event.getNewStart());
				String date_end_new=sdf.format(new Date(event.getNewStart().getTime()+currentDateRange));
				String className=e.getCaption();
				String classId_matiere="";
				try {
					ResultSet rs=con.queryTable("select id from matiere where nom='"+className+"'");
					while(rs.next()){
						classId_matiere=rs.getString("id");
					}
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				//System.out.println(className);	
				try {
					con.executeTable("update cours set date_debut='"+date_begin_new+"', date_fin='"+
				date_end_new+"' where date_debut='"+date_begin+"' and id_matiere='"+classId_matiere+"'");
					
				} catch (Exception e1) {					
					e1.printStackTrace();
				}
				
				}
			});
		}
		ResultSet rs = con.queryTable("select nom FROM matiere ");
		while(rs.next()){
			classSubjectInputField.addItem(rs.getString("nom"));
		}
		rs = con.queryTable("select nom FROM salles ");
		while(rs.next()){
			classroomInputField.addItem(rs.getString("nom"));
		}
		rs = con.queryTable("select type FROM type_cours ");
		while(rs.next()){
			classTypeInputField.addItem(rs.getString("type"));

		}
		
		classSubjectInputField.setNullSelectionItemId("Sélectionner");
		classroomInputField.setNullSelectionItemId("Sélectionner");
		classTypeInputField.setNullSelectionItemId("Sélectionner");

		classTeacherInputField.setNullSelectionItemId("Sélectionner");		
		classTimeInputField.setRequired(true);
		classTimeInputField.setRequiredError("The Field may not be empty.");
		classWeekInputField.setRequired(true);
		classWeekInputField.setRequiredError("The Field may not be empty.");

		classroomInputField.setRequired(true);
		classroomInputField.setRequiredError("The Field may not be empty.");
		classTypeInputField.setRequired(true);
		classTypeInputField.setRequiredError("The Field may not be empty.");
		moveMe.setVisible(false);	
		
		confirmAddPlanning.addListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				if(classSubjectInputField.getValue()!=null&&classTimeInputField.getValue()!=""&&classWeekInputField.getValue()!=""&&classroomInputField.getValue()!=null&&classTypeInputField.getValue()!=null){
					//moveMe.setWidth("20px");
					//moveMe.setHeight("30px");
					//System.out.println(classrommInputField.getValue());
					classSubjectInputField.setReadOnly(true);

					classTimeInputField.setReadOnly(true);
					classWeekInputField.setReadOnly(true);
					classroomInputField.setReadOnly(true);
					classTeacherInputField.setReadOnly(true);

					classTypeInputField.setReadOnly(true);
				moveMe.setVisible(true);	}
				else
				{

					getApplication().getMainWindow().showNotification(
							"Les champs doivent pas null.");
				}
			}
		});

		reset.addListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				moveMe.setVisible(false);
				classSubjectInputField.setReadOnly(false);
				classTimeInputField.setReadOnly(false);
				classWeekInputField.setReadOnly(false);
				classroomInputField.setReadOnly(false);
				classTeacherInputField.setReadOnly(false);

				classTypeInputField.setReadOnly(false);
				classSubjectInputField.setValue("");
				classTimeInputField.setValue("");
				classWeekInputField.setValue("");	
				classTypeInputField.setValue("");	

			}
		});

		hl.addComponent(cal);
		getTeacher();
		vl.addComponent(classTeacherInputField);
		vl.addComponent(classTeacherInputField);
		vl.addComponent(classSubjectInputField);
		vl.addComponent(classTimeInputField);
		vl.addComponent(classWeekInputField);

		vl.addComponent(classroomInputField);
		
		vl.addComponent(classTypeInputField);
		hl2.addComponent(confirmAddPlanning);
		hl2.addComponent(reset);
		vl.addComponent(hl2);
		vl.addComponent(moveMe);
		final DragAndDropWrapper classWrap = new DragAndDropWrapper(moveMe);
		classWrap.setDragStartMode(DragStartMode.COMPONENT);
		classWrap.setSizeUndefined();
		vl.addComponent(classWrap);
		hl.addComponent(vl);

	}
	
	private void getSubject(Object idTeacher) {
		try {
			con = new MysqlConnection();
			ResultSet rs = con.queryTable("SELECT m.Nom, m.ID" +
					" FROM matiere m" +
					" INNER JOIN matiere_prof p ON p.ID_matiere = m.ID" +
					" WHERE p.ID_prof = " + idTeacher);
			while (rs.next()) {
				String name = rs.getString("Nom");
				String id_matiere = rs.getString("ID");
				classSubjectInputField.addItem(id_matiere);
				classSubjectInputField.setItemCaption(id_matiere, name);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getTeacher() {
		try {
			con = new MysqlConnection();
			ResultSet rs;
				rs = con.queryTable("SELECT DISTINCT nom, prenom, id_identifiant"
						+ " FROM personne"
						+ " WHERE statut = 2");
			while (rs.next()) {
				String teacher = rs.getString("nom") + " " + rs.getString("prenom");
				String id_teacher = rs.getString("id_identifiant");
				classTeacherInputField.addItem(id_teacher);
				classTeacherInputField.setItemCaption(id_teacher, teacher);
				classTeacherInputField.addListener(new ValueChangeListener () {
					public void valueChange(ValueChangeEvent event) {
						// TODO Auto-generated method stub
						Object idTeacher = classTeacherInputField.getValue();
						getSubject(idTeacher);
					}
				});
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void changeWindow(BasicEvent e) throws Exception {
		getApplication().getMainWindow().removeWindow(change);
		change.setPositionX(500);
		change.setPositionY(200);
		final BasicEvent eFinal = e;
		HorizontalLayout hl = new HorizontalLayout();

		VerticalLayout vl = new VerticalLayout();	
		Button delete=new Button("Supprimer");
		Button confirm=new Button("Valider");
		final DateField beginDateField=new DateField();
		final DateField endDateField=new DateField();
		beginDateField.setDateFormat("dd/MM/yyyy  HH:mm");
		endDateField.setDateFormat("dd/MM/yyyy  HH:mm");
		final Select classNameField=new Select("Choix de la matière");
		Date beginDate=e.getStart();
		final String beginDateFinal=sdf.format(beginDate);
		Date endDate=e.getEnd();
		String className=e.getCaption();
		classNameField.setNullSelectionItemId("Sélectionner");
		ResultSet rs=con.queryTable("select nom from matiere");
		while(rs.next()){
			classNameField.addItem(rs.getString("nom"));
		}				
		ResultSet rs2=con.queryTable("select id from matiere where nom='"+className+"'");
		while(rs2.next()){
			className=rs2.getString("id");
		}
		final String classNameFinal=className;
		beginDateField.setValue(beginDate);
		endDateField.setValue(endDate);
		vl.addComponent(beginDateField);
		vl.addComponent(endDateField);
		vl.addComponent(classNameField);
		hl.addComponent(confirm);
		hl.addComponent(delete);
		vl.addComponent(hl);

		rs2=con.queryTable("select id FROM groupe where nom='"+nom+"'");
		int id_group=0;
		while(rs2.next()){
			id_group=Integer.parseInt(rs2.getString("id"));

		}
		final int id_groupFinal = id_group;
		delete.addListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				try {

					con.executeTable("delete from cours where id_matiere='"+classNameFinal+
							"' and date_debut='"+beginDateFinal+"' and id_groupe_cours="+id_groupFinal);
				} catch (Exception e) {					

					e.printStackTrace();
				}
				getApplication().getMainWindow().removeWindow(change);
				cal.removeEvent(eFinal);
			}
		});
		
		confirm.addListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				try {
					ResultSet rs2=con.queryTable("select id FROM matiere where nom='"+classNameField.getValue()+"'");
					
					String classId_matiereNew="";
					while(rs2.next()){
						classId_matiereNew=rs2.getString("id");
					}
					final String classId_matiereNewFinal=classId_matiereNew;
					con.executeTable("update cours set id_matiere='"+classId_matiereNewFinal+
							"', date_debut='"+sdf.format(beginDateField.getValue())+
							"', date_fin='"+sdf.format(endDateField.getValue())+"' where id_matiere='"+classNameFinal+
							"' and date_debut='"+beginDateFinal+"' and id_groupe_cours="+id_groupFinal);
				} catch (Exception e) {					

					e.printStackTrace();
				}
				getApplication().getMainWindow().removeWindow(change);
				cal.removeEvent(eFinal);
				cal.addEvent(new BasicEvent((String) classNameField.getValue(),
						"", (Date) beginDateField.getValue(),
						(Date) endDateField.getValue()));
			}
		});
		change.setContent(vl);
		change.setHeight(200);
		change.setWidth(180);
		getApplication().getMainWindow().addWindow(change);
	}

	protected void createEventByGroup(CalendarTargetDetails details,
			WrapperTransferable transferable) throws Exception {
		Date dropTime = details.getDropTime();
		java.util.Calendar timeCalendar = details.getTargetCalendar()
				.getInternalCalendar();
		timeCalendar.setTime(dropTime);

		timeCalendar.add(java.util.Calendar.HOUR, 32);//the original time is not the time i put on the calendar, there always exist 32 hours between them
		int week=Integer.parseInt(classWeekInputField.getValue().toString());
		for(int i=0;i<week;i++){		
		Date startTime=timeCalendar.getTime();
		timeCalendar.add(java.util.Calendar.MINUTE, Integer.parseInt(classTimeInputField.getValue().toString()));//
		Date endTime = timeCalendar.getTime();
		java.sql.Timestamp sqlDate_begin = new java.sql.Timestamp(startTime.getTime());
		//System.out.println(sqlDate_begin);
		java.sql.Timestamp sqlDate_end = new java.sql.Timestamp(endTime.getTime());
		//System.out.println(classrommInputField.getValue());		
		ResultSet rs=con.queryTable("select id from salles where nom='"+classroomInputField.getValue()+"'");
		int id_salle=0;
		while(rs.next()){
		id_salle=Integer.parseInt(rs.getString("id"));}				
		rs=con.queryTable("select id_identifiant from personne where personne.nom='"
		+classTeacherInputField.getValue()
		+"' and (personne.statut=2 or personne.statut=3)");
		int id_prof=0;
		while(rs.next()){
		id_prof=Integer.parseInt(rs.getString("id_identifiant"));}
		rs=con.queryTable("select id FROM groupe where nom='"+nom+"'");
		int id_group=0;
		while(rs.next()){
			id_group=Integer.parseInt(rs.getString("id"));
		}
		String id_matiere="";
		rs=con.queryTable("select id FROM matiere where nom='"+(String)classSubjectInputField.getValue()+"'");
		while(rs.next()){
			id_matiere=rs.getString("id");
		}
		int id_type=0;
		rs=con.queryTable("select id FROM type_cours where type='"+(String)classTypeInputField.getValue()+"'");
		while(rs.next()){
			id_type=Integer.parseInt(rs.getString("id"));
		}
		rs=con.queryTable("select id_matiere FROM matiere_groupe where id_groupe="+id_group+"");
		
		con.executeTable("INSERT INTO cours (id_matiere,Date_Debut,Date_Fin,Id_salle,id_professeur,id_groupe_cours,type,remarque) VALUES ('"
						+ id_matiere
						+ "','"
						+ sqlDate_begin
						+ "','"
						+ sqlDate_end + "',"+id_salle+","+id_prof+","+id_group+","+id_type+",'remarque')");
		BasicEventProvider ep = (BasicEventProvider) details
				.getTargetCalendar().getEventProvider();		
		ep.addEvent(new BasicEvent((String)classSubjectInputField.getValue(), "",
				startTime, endTime));
		timeCalendar.add(java.util.Calendar.MINUTE, 24*7*60-Integer.parseInt(classTimeInputField.getValue().toString()));//For adding several weeks planning

		}
	}

}
