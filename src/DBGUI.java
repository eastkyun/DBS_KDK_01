import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

public class DBGUI extends Application {
	private Connection con = MyConnection.makeConnection();
	Button[] buttons;
	private TableView<Object> table;
	TreeView<String> tree;
	Label[] labels;
	TextField[] txt;
	TextArea txtArea;
	private final String[] btntext = { "clear", "save", "update", "delete", "print", "search" };
	public static void main(String[] args) {
        launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		VBox vbox = new VBox();
		//vbox.setSpacing(20);
		vbox.setMinSize(900, 700);
		vbox.setMaxSize(1200, 1000);
		vbox.setPadding(new Insets(15, 15, 15, 15));
		vbox.setSpacing(10);
		vbox.setStyle("-fx-border-color: Black;");
		
		HBox tbox =addTopPane(stage);
		tbox.prefHeightProperty().bind(vbox.prefWidthProperty());
		vbox.getChildren().add(tbox);

		HBox cbox=addCenterPane();
        cbox.prefHeightProperty().bind(vbox.prefWidthProperty());
        vbox.getChildren().add(cbox);
		
        StackPane bbox = addBottomPane();
        bbox.prefHeightProperty().bind(vbox.prefWidthProperty());
        vbox.getChildren().add(bbox);

		Scene scene = new Scene(vbox);
		stage.setScene(scene);
		stage.setTitle("DBS_KDK_01 ");
		stage.show();
	}

	private StackPane addBottomPane() {
		StackPane stack = new StackPane();
		stack.setMaxHeight(150);
		stack.setMinHeight(150);
		
		stack.setStyle("-fx-border-color: #333333;");
		txtArea = new TextArea();
		txtArea.setEditable(false);
		txtArea.setWrapText(true);
		txtArea.setVisible(true);
		txtArea.prefHeightProperty().bind(stack.prefWidthProperty());
		txtArea.appendText(MyConnection.sb.toString()+"\n");
        stack.getChildren().add(txtArea);
        return stack;
		
	}

	private HBox addTopPane(Stage stage) {
		HBox hbox = new HBox();
		hbox.setPadding(new Insets(15,15,15,15));
		hbox.setSpacing(10);
		hbox.setStyle("-fx-border-color: Blue;");
		
		buttons = new Button[btntext.length];
		for(int i=0;i<buttons.length;i++) {
			buttons[i] = new Button(btntext[i]);
			buttons[i].setPrefSize(80, 20);
			buttons[i].prefHeightProperty().bind(hbox.prefHeightProperty());	
		}
		// 버튼 기능 추가할 부분
		for(int i=0;i<buttons.length;i++) {
        	final int j=i;
        	buttons[j].setOnAction((event)->{
        		String str = buttons[j].getText();
        		txtArea.appendText("You have selected " + str + "\n");
        		if("clear".equals(str)) {
        			clearTextFields();
        		}
        		else if("save".contentEquals(str)) {
        			if(mySelectedNode().equals(Nodes.Club.toString()))addRecord("Club");
        			else if(mySelectedNode().equals(Nodes.Member.toString()))addRecord("Member");
        			else if(mySelectedNode().equals(Nodes.Professor.toString()))addRecord("Professor");
        			else if(mySelectedNode().equals(Nodes.Activity.toString()))addRecord("Activity");
        			else if(mySelectedNode().equals(Nodes.JoinClub.toString()))addRecord("JoinClub");
        		}
        		else if("update".contentEquals(str)) {
        			if(mySelectedNode().equals(Nodes.Club.toString()))updateSP("Club");
        			else if(mySelectedNode().equals(Nodes.Member.toString()))updateSP("Member");
        			else if(mySelectedNode().equals(Nodes.Professor.toString()))updateSP("Professor");
        			else if(mySelectedNode().equals(Nodes.Activity.toString()))updateSP("Activity");
        			else if(mySelectedNode().equals(Nodes.JoinClub.toString()))updateSP("JoinClub");
        			
        		}
        		else if("delete".contentEquals(str)) {
        			if(mySelectedNode().equals(Nodes.Club.toString())) deleteSP("Club");
        			else if(mySelectedNode().equals(Nodes.Member.toString()))deleteSP("Member");
        			else if(mySelectedNode().equals(Nodes.Professor.toString()))deleteSP("Professor");
        			else if(mySelectedNode().equals(Nodes.Activity.toString()))deleteSP("Activity");
        			else if(mySelectedNode().equals(Nodes.JoinClub.toString()))deleteSP("JoinClub");
        		}
        		else if("print".contentEquals(str)) {
        			if(mySelectedNode().equals(Nodes.Club.toString())) printReport("club");
        			else if(mySelectedNode().equals(Nodes.Member.toString()))printReport("member");
        			else if(mySelectedNode().equals(Nodes.Professor.toString()))printReport("professor");
        			else if(mySelectedNode().equals(Nodes.Activity.toString()))printReport("activity");
        			else if(mySelectedNode().equals(Nodes.JoinClub.toString()))printReport("joinClub");
        			
        		}
				
        	});
        }
		hbox.getChildren().addAll(buttons);		
		return hbox;
	}	

	private void updateSP(String tName) {
		int nCol = table.getColumns().size(); 
		String sql = "{call update"+ tName +"(";
		for(int i=1;i<nCol;i++) sql += "?,";
		sql += "?)}";
		try {
			CallableStatement cst = con.prepareCall(sql);
			for(int i=1;i<=nCol;i++) {
				cst.setString(i, txt[i-1].getText());
			}
			cst.execute();
			cst.close();
			txtArea.appendText(" The selected record is updated " + "\n");
		}
		catch(Exception e) {
	    	txtArea.appendText("Record is not updated..."+"\n");
	    	txtArea.appendText(e.getMessage()+ "\n");
	    }finally {}
	}
	
	private void deleteSP(String tName) {
		String query = "SELECT Count(*) AS NOP FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE" + 
						" WHERE TABLE_NAME='"+ tName+"' and CONSTRAINT_NAME like 'PK%'";
		
		try {
			PreparedStatement pst = con.prepareStatement(query);
			ResultSet rs = pst.executeQuery();
			rs.next();
			int NOP = Integer.parseInt(rs.getString("NOP").toString());
			rs.close();
			
			System.out.println(query);
			String sql="{call delete"+ tName +"(";
			for(int i=1;i<NOP;i++) sql +="?,";
			sql += "?)}";			
			System.out.println(sql);
			CallableStatement cst = con.prepareCall(sql);
			for(int i=1;i<=NOP;i++) {
				cst.setString(i, txt[i-1].getText());
			}			
			cst.execute();
			txtArea.appendText(" record(s) are deleted " + "\n");
			cst.close();
			
			pst.close();
		}catch(Exception e) {
	    	txtArea.appendText("Record is not deleted..."+e.getMessage()+"\n");
	    	
	    }finally {}
		
	}

	private void addRecord(String tName){
		String query = "insert into " + tName+" values(";
		int nCol = table.getColumns().size();
		for(int i=1;i<nCol;i++){
			query += "?, ";
		}
		query += "?)";
		
		String query2 = "select COLUMN_NAME, DATA_TYPE from "
				+ "INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='" + tName+"'";
		try{
			PreparedStatement pst = con.prepareStatement(query2);
			ResultSet rs = pst.executeQuery();	
			pst = con.prepareStatement(query);
					
			rs.next();
			for(int i=1;i<=nCol;i++,rs.next()){
				String col = rs.getString("DATA_TYPE").toString();
				System.out.println(i + " - " + col);
				if("int".equals(col)){ 
					pst.setInt(i, Integer.parseInt(txt[i-1].getText().toString()));
				}
				else if("float".equals(col)){
					pst.setFloat(i, Float.parseFloat(txt[i-1].getText().toString()));
				}
				else{
					pst.setString(i, txt[i-1].getText().toString());
				}				
			}
			int addNum = pst.executeUpdate();
			txtArea.appendText(addNum + "row(s) added \n");
			pst.close();
			rs.close();
		}catch(Exception e){
		txtArea.appendText(e.getMessage()+ "\n");
		}
	}
    private void clearTextFields() {
    	int numberOfColunm = table.getColumns().size();
    	for(int i =0;i<numberOfColunm;i++) {
    		txt[i].setText(" ");
    	}
    }

	private HBox addCenterPane() {
		HBox hb1 = new HBox();
		
		// add TableView
		VBox vb = new VBox();
		
		table = new TableView<>();
		table.setMinSize(800, 300);
		table.setMaxSize(800, 300);
		table.setStyle("-fx-border-color: Black;");
		table.prefWidthProperty().bind(vb.prefWidthProperty());
		table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		table.getSelectionModel().setCellSelectionEnabled(false);
		
		// 테이블 클릭시 이벤트 추가할 것
        table.getSelectionModel().selectedItemProperty().addListener((obs,oldValue, newValue)->{
        	if(oldValue!=newValue) showFields();
        });
        
		GridPane gp = new GridPane();
		gp.setPadding(new Insets(15,15,15,125));
		gp.setHgap(10);
		gp.setVgap(10);
		gp.setStyle("-fx-border-color:Blue;");
		gp.prefHeightProperty().bind(table.prefHeightProperty());
		txt = new TextField[10];
		labels = new Label[10];
		
		for(int i=0;i<labels.length;i++) {
			labels[i] = new Label("Label...");
			labels[i].setMinSize(150, 25);
			txt[i] = new TextField("Text...");
			txt[i].setMinSize(300, 25);
			gp.addRow(i, labels[i], txt[i]);
			labels[i].prefHeightProperty().bind(gp.widthProperty());
			txt[i].prefHeightProperty().bind(gp.widthProperty());
		}
		vb.getChildren().addAll(table,gp);
		
		// Add TreeView
		StackPane stack = new StackPane();
		
		tree = addNodesToTree();
		tree.setShowRoot(true);
		
        tree.getSelectionModel().selectedItemProperty().addListener((v,oldValue, newValue)->{
        	if(newValue != oldValue) {
            	String str=mySelectedNode();
            	txtArea.appendText("You have selected  " + str+"\n");
            	if(str.equals(Nodes.Activity.toString()) || str.equals(Nodes.Club.toString()) || 
            	   str.equals(Nodes.Member.toString()) || str.equals(Nodes.Professor.toString())||
            	   str.equals(Nodes.JoinClub.toString())) 
            		rsToTableView(str);
            }
        	
        });
        
		
		tree.setMaxWidth(200);
		tree.prefWidthProperty().bind(stack.prefWidthProperty());
		stack.getChildren().add(tree);
		
		hb1.getChildren().addAll(stack, vb);
        hb1.setStyle("-fx-border-color:black;");
        hb1.setSpacing(20);
        hb1.prefHeightProperty().bind(vb.prefWidthProperty());
		return hb1;
	}

    @SuppressWarnings("unchecked")
	private void rsToTableView(String s) {
    	table.getColumns().clear();
		for ( int i = 0; i<table.getItems().size(); i++) {
		   table.getItems().clear();
		}
		
		ObservableList<Object> data = FXCollections.observableArrayList();
		try {
			String query = "select * from " + s + "";
			PreparedStatement pst = null;
			pst = con.prepareStatement(query);
			ResultSet rs = pst.executeQuery();

			ResultSetMetaData rsmd = rs.getMetaData();

			int colCount = rsmd.getColumnCount();

			// get data rows

			for (int i = 0; i < colCount; i++) {

				int dataType = rsmd.getColumnType(i + 1);

				final int j = i;
				TableColumn col = new TableColumn(rsmd.getColumnName(i + 1));

				col.setCellValueFactory(
						new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
							public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
								return new SimpleStringProperty(param.getValue().get(j).toString());
							}
						});

				table.getColumns().addAll(col);
			}
			while (rs.next()) {
				ObservableList<String> row = FXCollections.observableArrayList();
				for (int k = 1; k <= colCount; k++) {
					String str1=rs.getString(k);
					if(rs.getString(k)==null)
						str1="null";
					row.add(str1);
				}
					
				data.add(row);
			}

			table.setItems(data);
			
			table.getSelectionModel().select(0);

			showFields();
			

		} catch (Exception e) {
			System.err.println(e.getMessage()+ "\n");
		} finally {
		}
		
	}

	private String mySelectedNode() {
    	TreeItem ti= tree.getSelectionModel().selectedItemProperty().getValue();
    	return ti.getValue().toString();
    }


	private void showFields() {
		clearFields();
		Integer row;
		try {
			TablePosition pos = (TablePosition) table.getSelectionModel().getSelectedCells().get(0);
			row = pos.getRow();
		}catch(Exception e) {
			row = 0;
		}finally {}
//		txtArea.appendText(row.toString() + "\n");
		int cols = table.getColumns().size();
		
		for(int j=0;j<cols;j++) {
			Object ch = ((TableColumnBase<?, ?>) table.getColumns().get(j)).getText();
    		Object cell = ((TableColumnBase<?, ?>) table.getColumns().get(j)).getCellData(row).toString();
    		if(cell==null) {
    			txt[j].setText("");
    		}
    		else {
    			txt[j].setText(cell.toString());
    			txt[j].setVisible(true);
    		}
    		labels[j].setText(ch.toString());
    		labels[j].setVisible(true);
		}
	}
	
    private void clearFields() {
    	for(int i=0;i<txt.length;i++) {
    		txt[i].setText("");
    		txt[i].setVisible(false);
    		labels[i].setText("");
    		labels[i].setVisible(false);
    	}
    }
    
	@SuppressWarnings("unchecked")
	private TreeView<String> addNodesToTree() {
		TreeView<String> tree = new TreeView<String>();
		
		TreeItem<String> root, tables, procedures, functions, exit, about;
		root = new TreeItem<String>("DBS_KDK_01");
		
		tables = new TreeItem<String>("Tables");
		makeChild(Nodes.Club.toString(),tables);
		makeChild(Nodes.Member.toString(),tables);
		makeChild(Nodes.Professor.toString(),tables);
		makeChild(Nodes.Activity.toString(),tables);
		makeChild(Nodes.JoinClub.toString(),tables);
		 
		functions = new TreeItem<String>("Functions");
		makeChild(Nodes.getActivity.toString(), functions);
		makeChild(Nodes.getCategoryClub.toString(), functions);
		makeChild(Nodes.getClubDetail.toString(), functions);
		makeChild(Nodes.getClubInfo.toString(), functions);
		makeChild(Nodes.getMembers.toString(), functions);
		
		
		procedures = new TreeItem<String>("Stored Procedure");
		makeChild(Nodes.deleteActivity.toString(), procedures);
		makeChild(Nodes.deleteClub.toString(), procedures);
		makeChild(Nodes.deleteJoinClub.toString(), procedures);
		makeChild(Nodes.deleteMember.toString(), procedures);
		makeChild(Nodes.deleteProfessor.toString(), procedures);
		makeChild(Nodes.insertActivity.toString(), procedures);
		makeChild(Nodes.insertClub.toString(), procedures);
		makeChild(Nodes.insertJoinClub.toString(), procedures);
		makeChild(Nodes.insertMember.toString(), procedures);
		makeChild(Nodes.insertProfessor.toString(), procedures);
		makeChild(Nodes.updateActivity.toString(), procedures);
		makeChild(Nodes.updateClub.toString(), procedures);
		makeChild(Nodes.updateJoinClub.toString(), procedures);
		makeChild(Nodes.updateMember.toString(), procedures);
		makeChild(Nodes.updateProfessor.toString(), procedures);
		
		
		exit = new TreeItem<String>(Nodes.Exit.toString());
		about = new TreeItem<String>(Nodes.About.toString());
		root.getChildren().addAll(tables, procedures, functions , exit, about);
		tree.setRoot(root);
		return tree;
	}

	private TreeItem<String> makeChild(String title, TreeItem<String> parent) {
		TreeItem<String> item = new TreeItem<>(title);
		item.setExpanded(false);
		parent.getChildren().add(item);
		return item;		
	}
    private void printReport(String tName) {
    	String fileName = tName + "Report.jrxml";
    	try {
    		JasperReport jr = JasperCompileManager.compileReport(fileName);
    		JasperPrint jp = JasperFillManager.fillReport(jr, null, con);
    		JasperViewer.viewReport(jp);
    	}catch(Exception e) {
    		txtArea.appendText("There is a problem in printing" + e.getMessage()+ "\n");
    	}
    }

}
