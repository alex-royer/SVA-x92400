/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Modèle;

/*
 * 
 * Librairies importées
 */
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 * 
 * Connexion a votre BDD locale ou à distance sur le serveur de l'ECE via le tunnel SSH
 * 
 * @author segado
 */
public class Connexion extends java.lang.Object{

    /**
     * Attributs prives : connexion JDBC, statement, ordre requete et resultat
     * requete
     */
    private Connection conn;
    private Statement stmt;
    private ResultSet rset;
    private ResultSetMetaData rsetMeta;
    /**
     * ArrayList public pour les tables
     */
    public java.util.ArrayList<java.lang.String> tables = new ArrayList<>();
    /**
     * ArrayList public pour les requêtes de sélection
     */
    public java.util.ArrayList<java.lang.String> requetes = new ArrayList<>();
    /**
     * ArrayList public pour les requêtes de MAJ
     */
    public java.util.ArrayList<java.lang.String> requetesMaj = new ArrayList<>();

    /**
     * Constructeur avec 3 paramètres : nom, login et password de la BDD locale
     *
     * @param nameDatabase
     * @param loginDatabase
     * @param passwordDatabase
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     */
    public Connexion(String nameDatabase, String loginDatabase, String passwordDatabase) throws SQLException, ClassNotFoundException {
        // chargement driver "com.mysql.jdbc.Driver"
        Class.forName("com.mysql.jdbc.Driver");

        // url de connexion "jdbc:mysql://localhost:3305/usernameECE"
        String urlDatabase = "jdbc:mysql://localhost/" + nameDatabase;

        //création d'une connexion JDBC à la base 
        conn = DriverManager.getConnection(urlDatabase, loginDatabase, passwordDatabase);

        // création d'un ordre SQL (statement)
        stmt = conn.createStatement();
    }

    /**
     * Constructeur avec 4 paramètres : username et password ECE, login et
     * password de la BDD à distance sur le serveur de l'ECE
     * @param usernameECE
     * @param passwordECE
     * @param loginDatabase
     * @param passwordDatabase
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     */
    public Connexion(java.lang.String usernameECE, java.lang.String passwordECE, java.lang.String loginDatabase, java.lang.String passwordDatabase) throws SQLException, ClassNotFoundException {
        // chargement driver "com.mysql.jdbc.Driver"
        Class.forName("com.mysql.jdbc.Driver");

        // Connexion via le tunnel SSH avec le username et le password ECE
        SSHTunnel ssh = new SSHTunnel(usernameECE, passwordECE);

        if (ssh.connect()) {
            System.out.println("Connexion reussie");

            // url de connexion "jdbc:mysql://localhost:3305/usernameECE"
            String urlDatabase = "jdbc:mysql://localhost:3305/" + usernameECE;

            //création d'une connexion JDBC à la base
            conn = DriverManager.getConnection(urlDatabase, loginDatabase, passwordDatabase);

            // création d'un ordre SQL (statement)
            stmt = conn.createStatement();

        }
    }

    /**
     * Méthode qui ajoute la table en parametre dans son ArrayList
     *
     * @param table
     */
    public void ajouterTable(String table) {
        tables.add(table);
    }

    /**
     * Méthode qui ajoute la requete de selection en parametre dans son
     * ArrayList
     *
     * @param requete
     */
    public void ajouterRequete(String requete) {
        requetes.add(requete);
    }

    /**
     * Méthode qui ajoute la requete de MAJ en parametre dans son
     * ArrayList
     *
     * @param requete
     */
    public void ajouterRequeteMaj(String requete) {
        requetesMaj.add(requete);
    }

    /**
     * Méthode qui retourne l'ArrayList des champs de la table en parametre
     *
     * @param table
     * @return
     * @throws java.sql.SQLException
     */
    public java.util.ArrayList remplirChampsTable(java.lang.String table) throws java.sql.SQLException {
        // récupération de l'ordre de la requete
        rset = stmt.executeQuery("select * from " + table);

        // récupération du résultat de l'ordre
        rsetMeta = rset.getMetaData();

        // calcul du nombre de colonnes du resultat
        int nbColonne = rsetMeta.getColumnCount();

        // creation d'une ArrayList de String
        ArrayList<String> liste;
        liste = new ArrayList<>();
        String champs = "";
        // Ajouter tous les champs du resultat dans l'ArrayList
        for (int i = 0; i < nbColonne; i++) {
            champs = champs + " " + rsetMeta.getColumnLabel(i + 1);
        }

        // ajouter un "\n" à la ligne des champs
        champs = champs + "\n";

        // ajouter les champs de la ligne dans l'ArrayList
        liste.add(champs);

        // Retourner l'ArrayList
        return liste;
    }

    /**
     * Methode qui retourne l'ArrayList des champs de la requete en parametre
     * @param requete
     * @return 
     * @throws java.sql.SQLException
     */
    public java.util.ArrayList remplirChampsRequete(java.lang.String requete) throws java.sql.SQLException {
        // récupération de l'ordre de la requete
        rset = stmt.executeQuery(requete);

        // récupération du résultat de l'ordre
        rsetMeta = rset.getMetaData();

        // calcul du nombre de colonnes du resultat
        int nbColonne = rsetMeta.getColumnCount();

        // creation d'une ArrayList de String
        ArrayList<String> liste;
        liste = new ArrayList<String>();

        // tant qu'il reste une ligne 
        while (rset.next()) {
            String champs;
            champs = rset.getString(1); // ajouter premier champ

            // Concatener les champs de la ligne separes par ,
            for (int i = 1; i < nbColonne; i++) {
                champs = champs + "," + rset.getString(i + 1);
            }

            // ajouter un "\n" à la ligne des champs
            champs = champs + "\n";

            // ajouter les champs de la ligne dans l'ArrayList
            liste.add(champs);
        }

        // Retourner l'ArrayList
        return liste;
    }

    /**
     * Méthode qui execute une requete de MAJ en parametre
     * @param requeteMaj
     * @throws java.sql.SQLException
     */
    public void executeUpdate(java.lang.String requeteMaj) throws java.sql.SQLException {
        stmt.executeUpdate(requeteMaj);
    }
    
    public ArrayList<String> getListe()
    {
        return requetes;
    }
    
      public void afficherCamembert2() throws SQLException
    {
        String requete ="SELECT * FROM CHAMBRE ";
         rset = stmt.executeQuery(requete);

        // récupération du résultat de l'ordre
        rsetMeta = rset.getMetaData();
       DefaultCategoryDataset data=new DefaultCategoryDataset();
       //   DefaultPieDataset my_pie_chart_dataset = new DefaultPieDataset();
      //  String requete ="SELECT nb_lits FROM CHAMBRE ";
                ajouterRequete(requete);
                   
                   int success_rate=0;
                   int oldrea=0;
                   int oldchir=0;
                   int oldcar=0;
                  while (rset.next()) {
                      
                      switch(rset.getString("CODE_SERVICE"))
                      {
                          case "REA":
                                String state = rset.getString("CODE_SERVICE");
                                 success_rate = rset.getInt("NB_LITS")+oldrea;
                                 oldrea=success_rate;
                                data.setValue(success_rate,"s1",state);
                                break;
                                
                                         case "CAR":
                                String state2 = rset.getString("CODE_SERVICE");
                                 success_rate = rset.getInt("NB_LITS")+oldcar;
                                 oldcar=success_rate;
                                data.setValue(success_rate,"s1",state2);
                                break;
                                         case "CHG":
                                String state3 = rset.getString("CODE_SERVICE");
                                 success_rate = rset.getInt("NB_LITS")+oldchir;
                                 oldchir=success_rate;
                                data.setValue(success_rate,"s1",state3);
                                break;
                           
                                //my_pie_chart_dataset.setValue(state, success_rate); //Convert data source from table to Pie Chart Data Source                               
                                }
                  }
        //        DefaultCategoryDataset mydataset=new DefaultCategoryDataset();        
   // DefaultPieDataset dataset = new DefaultPieDataset();
    /*dataset.setValue("Category 1", 43.2);
dataset.setValue("Category 2", 27.9);
dataset.setValue("Category 3", 79.5);*/
JFreeChart chart; // URLs?
        chart = ChartFactory.createBarChart(
              "Nombre de lit par Secteur", // chart title
"Category", // domain axis label
"Nombre de lit", // range axis label

                data,
               PlotOrientation.VERTICAL, // orientation
false, // include legend
true, // tooltips?
false // URLs?
);
ChartFrame frame = new ChartFrame("Test", chart);
frame.pack();
frame.setVisible(true);
    }
      
      
        public void afficherCamembert1() throws SQLException
    {
        String requete ="SELECT * FROM DOCTEUR";
         rset = stmt.executeQuery(requete);

        // récupération du résultat de l'ordre
        rsetMeta = rset.getMetaData();
   
         DefaultPieDataset data = new DefaultPieDataset();
      
                ajouterRequete(requete);
              
                   int cptTraum=0;
        
                   int cptCar=0;
                   int cptPneu=0;
                   int cptRadio=0;
                   int cptAnes=0;
                   int cptOrtho=0;
                  while (rset.next()) {
                      
                      switch(rset.getString("specialite"))
                      {
                          case "Traumatologue":
                               
                              cptTraum++;
                               
                                break;
                                
                                        case "Cardiologue":
                               cptCar++;
                                break;
                                        case "Pneumologue":
                                            cptPneu++;
                                            break;                       
                                
                               case "Radiologue":
                                            cptRadio++;
                                            break;     
                                            
                                                         case "Orthopediste":
                                            cptOrtho++;
                                            break;     
                                                         case "Anesthesiste":
                                            cptAnes++;
                                            break;     
                                }
                  
                  }
              

                  data.setValue("Traumatologue",cptTraum);
                   data.setValue("Cardiologue",cptCar);
                   data.setValue("Pneumologue",cptPneu);
                   data.setValue("Radiologue",cptRadio);
                   data.setValue("Orthopediste",cptOrtho);
                   data.setValue("Anesthesiste",cptAnes);
                   
                  
        //        DefaultCategoryDataset mydataset=new DefaultCategoryDataset();        
   // DefaultPieDataset dataset = new DefaultPieDataset();
    /*dataset.setValue("Category 1", 43.2);
dataset.setValue("Category 2", 27.9);
dataset.setValue("Category 3", 79.5);*/
JFreeChart chart= ChartFactory.createPieChart(
        
              "Camembert selon la spécialité des médecins", // chart title
                data,
          
false, // include legend
true, // tooltips?
false // URLs?
);
PiePlot plot = (PiePlot) chart.getPlot();

plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}={1} ({2})"));


//PiePlot3D plot3 = (PiePlot3D) chart.getPlot();
plot.setExplodePercent("Cardiologue",0.30);
// plot3.setForegroundAlpha(0.7f);
ChartFrame frame = new ChartFrame("Test", chart);

frame.pack();
frame.setVisible(true);
    }

      
      public Connection getConnexion(){
          return this.conn;
      }
      
      
      public ResultSet getResultSet(){
          return this.rset;
      }
      
       public Statement getStatement(){
          return this.stmt;
      }
}
