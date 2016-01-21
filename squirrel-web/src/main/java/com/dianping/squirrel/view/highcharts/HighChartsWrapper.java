package com.dianping.squirrel.view.highcharts;


public class HighChartsWrapper {
	
	private String title;
	
	private String subTitle;
	
	private String xAxisTitle = "时间";
	
	private String yAxisTitle = "QPS";
	
	private Series [] series;
	
	private PlotOption plotOption;
	

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	public Series[] getSeries() {
		return series;
	}

	public void setSeries(Series[] series) {
		this.series = series;
	}

	public PlotOption getPlotOption() {
		return plotOption;
	}

	public void setPlotOption(PlotOption plotOption) {
		this.plotOption = plotOption;
	}

	public String getyAxisTitle() {
		return yAxisTitle;
	}

	public void setyAxisTitle(String yAxisTitle) {
		this.yAxisTitle = yAxisTitle;
	}

	/**
	 * @return the xAxisTitle
	 */
	public String getxAxisTitle() {
		return xAxisTitle;
	}

	/**
	 * @param xAxisTitle the xAxisTitle to set
	 */
	public void setxAxisTitle(String xAxisTitle) {
		this.xAxisTitle = xAxisTitle;
	}

	public static class Series{
		
		private String name;
		private Number[] data;

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Number[] getData() {
			return data;
		}
		public void setData(Number[] ls) {
			this.data = ls;
		}
	}

	public static class PlotOption{
		
		private PlotOptionSeries series;

		public PlotOptionSeries getSeries() {
			return series;
		}

		public void setSeries(PlotOptionSeries series) {
			this.series = series;
		}
	}
	
	public static class PlotOptionSeries{
		
		/**
		 * 图表开始时间(毫秒)
		 */
        private long pointStart;
        
		/**
         * 图标时间间隔（毫秒）
         */
        private long pointInterval;
        
        public long getPointStart() {
			return pointStart;
		}

		public void setPointStart(long pointStart) {
			this.pointStart = pointStart;
		}

		public long getPointInterval() {
			return pointInterval;
		}

		public void setPointInterval(long pointInterval) {
			this.pointInterval = pointInterval;
		}
	}
}
