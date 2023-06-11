package tuanv20.mockjiraapi.Model;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Linechart {
    private static final String IMAGE_PATH = "C:\\Users\\rebed\\Work\\mock-jira-api\\contact-png\\";
    String img;
    Issue issue;

    public Linechart(String img, Issue issue){
        this.img = img;
        this.issue = issue;
    }

    public void createLineChart() throws IOException{
        JFreeChart lineChart = ChartFactory.createLineChart(
         "Title",
         "Seconds","Telemetry Frames",
         createDataset(),
         PlotOrientation.VERTICAL,
         true,true,false);
         File f = new File(IMAGE_PATH + this.img);
         lineChart.removeLegend();
         ChartUtils.saveChartAsPNG(f, lineChart, 600, 600);
    }

    private DefaultCategoryDataset createDataset( ) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(Data datapoint : issue.getData()){
            dataset.addValue( datapoint.getTlm_fr() , "Contact" , df.format(new Date(datapoint.getTime())));
        }
        return dataset;
    }
}