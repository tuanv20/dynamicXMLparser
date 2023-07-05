package tuanv20.mockjiraapi.Model;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException; 

@Component
public class Linechart {
    JFreeChart linechart;
    @Value("${paths.img_path}")
    private String IMG_DIR_PATH;

    public Linechart(){
    }
    

    public void createLineChart(JIRAIssue issue) {
        this.linechart = ChartFactory.createLineChart(
        "Telemetry Frames v. Seconds",
        "Seconds","Telemetry Frames",
        createDataset(issue),
        PlotOrientation.VERTICAL,
        true,true,false);
        int first_tlm = issue.getData().get(0).getTlm_fr();
        this.linechart.getCategoryPlot().getRangeAxis().setLowerBound(first_tlm);
    }

    private DefaultCategoryDataset createDataset(JIRAIssue issue) {
        long contact_start = issue.getFirstClass().getAOS();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        for(Data datapoint : issue.getData()){
            long sec_from_start = (datapoint.getTime() - contact_start) / 1000;
            dataset.addValue(datapoint.getTlm_fr() , "Contact" , String.valueOf(sec_from_start));
        }
        return dataset;
    }

    public File exportAsPng(String img_name) throws IOException{
        File f = new File(IMG_DIR_PATH + "/" + img_name);
        this.linechart.removeLegend();
        ChartUtils.saveChartAsPNG(f, this.linechart, 600, 600);
        return f;
    }
}