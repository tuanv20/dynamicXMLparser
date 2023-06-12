package tuanv20.mockjiraapi.Model;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import java.io.File;
import java.io.IOException; 

public class Linechart {
    private static final String IMAGE_PATH = "C:\\Users\\rebed\\Work\\mock-jira-api\\contact-png\\";
    String img;
    Issue issue;
    JFreeChart linechart;

    public Linechart(String img, Issue issue){
        this.img = img;
        this.issue = issue;
        createLineChart();
    }
    

    public void createLineChart() {
        String issue_id = this.issue.FirstClass().getID();
        this.linechart = ChartFactory.createLineChart(
         "Contact " + issue_id,
         "Seconds","Telemetry Frames",
         createDataset(),
         PlotOrientation.VERTICAL,
         true,true,false);
        int first_tlm = issue.getData().get(0).getTlm_fr();
        this.linechart.getCategoryPlot().getRangeAxis().setLowerBound(first_tlm);
    }

    private DefaultCategoryDataset createDataset( ) {
        long contact_start = this.issue.FirstClass().getAOS();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        for(Data datapoint : issue.getData()){
            long sec_from_start = (datapoint.getTime() - contact_start) / 1000;
            dataset.addValue( datapoint.getTlm_fr() , "Contact" , String.valueOf(sec_from_start));
        }
        return dataset;
    }

    public void exportAsPng() throws IOException{
        File f = new File(IMAGE_PATH + this.img);
         this.linechart.removeLegend();
         ChartUtils.saveChartAsPNG(f, this.linechart, 600, 600);
    }
}