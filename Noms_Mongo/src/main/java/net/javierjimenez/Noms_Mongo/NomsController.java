package net.javierjimenez.Noms_Mongo;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

/**
 * 
 * @author alumne1daw
 *
 */
public class NomsController implements Initializable {

	/**
	 * Objeto TextField que captura el valor escrito que corresponde a un
	 * nombre.
	 */
	@FXML
	private TextField txtNom;

	/**
	 * Objeto TextField que captura el valor escrito que corresponde a un Santo.
	 */
	@FXML
	private TextField txtSant;

	/**
	 * Objeto Button que inicia la busqueda en la base de datos del nombre
	 * escrito.
	 */
	@FXML
	private Button btnSant;

	/**
	 * Objeto Button que inicia la busqueda en la base de datos del Santo
	 * escrito.
	 */
	@FXML
	private Button btnNom;

	/**
	 * Objeto Label donde añadiremos la informacion encontrada tras la busqueda
	 * del nombre en nuestra base de datos.
	 */
	@FXML
	private Label diaSant;

	/**
	 * Objeto ListView donde añadiremos la informacion encontrada en una lista
	 * tras la busqueda del Santo en nuestra base de datos.
	 */
	@FXML
	private ListView<String> llistaNoms;

	/**
	 * Objeto MongoClient que establece la conexion con la base de datos.
	 */
	private MongoClient client;

	/**
	 * Objeto MongoCollection que contiene todas las colecciones de la base de
	 * datos que hemos elegido.
	 */
	private MongoCollection<Document> col;

	/**
	 * Metode que inicialitza la connexio amb la Base de Dades.
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		client = new MongoClient(new MongoClientURI("mongodb://admin:admin@ds017231.mlab.com:17231/noms_mongodb"));

		MongoDatabase db = client.getDatabase("noms_mongodb");

		col = db.getCollection("noms");
	}

	/**
	 * Metodo encargado de buscar los dias que se celebra el santo del nombre
	 * que hemos escrito.
	 * 
	 * @param event Objecte ActionEvent
	 */
	@SuppressWarnings("unchecked")
	public void buscarSant(ActionEvent event) {

		diaSant.setText(" ");

		Document doc = col.find(or(eq("catala", txtNom.getText()), eq("castella", txtNom.getText()))).first();

		if (doc == null) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Problema de Búsqueda");
			alert.setHeaderText("ALERTA: Problema en la búsqueda");
			alert.setContentText("Los datos escritos son erróneos o\nno existe dicho nombre.\nRehaga la búsqueda.");
			alert.showAndWait();
		} else {

			List<java.lang.String> sants = (List<String>) doc.get("sants");

			if ((sants.size() == 1 && (sants.get(0).equals("1 de gener") || sants.get(0).equals("01 de gener")))
					|| doc.get("sants") == null) {

				String observacio = (String) doc.get("observacions");

				if (observacio != null) {

					diaSant.setText(observacio);

				} else {

					diaSant.setText("No hi ha cap observació");

				}
			} else {

				String valor = "";

				for (String data : sants) {

					valor = valor + "- " + data + " -\n";
				}
				diaSant.setText(valor);
			}
		}
	}

	/**
	 * Metodo encargado de buscar los nombres que celebran el santo en la fecha
	 * que hemos escrito.
	 * 
	 * @param event Objecte ActionEvent
	 */
	public void buscarNoms(ActionEvent event) {

		List<Document> noms = new ArrayList<Document>();
		ObservableList<String> lista_nombres = FXCollections.observableArrayList();
		MongoCursor<Document> cur = col.find(in("sants", txtSant.getText())).iterator();

		while (cur.hasNext()) {
			noms.add(cur.next());
		}
		for (int i = 0; i < noms.size(); i++) {
			String cat = (String) noms.get(i).get("catala");
			String cast = (String) noms.get(i).get("castella");

			if (cast == null) {
				cast = "No existe";
			} else if (cat == null) {
				cat = "No existeix";
			}
			lista_nombres.add("Catala: " + cat + " - Castella: " + cast);
		}
		if (lista_nombres.isEmpty()) {

			llistaNoms.getItems().clear();

			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Problema de Búsqueda");
			alert.setHeaderText("ALERTA: Problema en la búsqueda");
			alert.setContentText("Los datos escritos son erróneos o\nno existe dicho Santo.\nRehaga la búsqueda.");
			alert.showAndWait();

		} else {
			llistaNoms.setItems(lista_nombres);
		}
		cur.close();
	}
}
