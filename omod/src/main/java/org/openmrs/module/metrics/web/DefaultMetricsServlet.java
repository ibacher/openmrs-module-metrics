package org.openmrs.module.metrics.web;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.SortedMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codahale.metrics.Metric;
import io.micrometer.jmx.JmxMeterRegistry;
import org.openmrs.module.metrics.api.exceptions.MetricsException;
import org.openmrs.module.metrics.util.MetricHandler;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class DefaultMetricsServlet extends HttpServlet {
	
	MetricHandler metricHandler;
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		LocalDateTime startDatetime;
		LocalDateTime endDatetime;
		JmxMeterRegistry meterRegistry;
		final String CONTENT_TYPE = "application/json";

		if (req.getParameter("startDateTime") != null && req.getParameter("endDateTime") != null) {
			startDatetime = LocalDateTime.parse(req.getParameter("startDateTime"));
			endDatetime = LocalDateTime.parse(req.getParameter("endDatetime"));
			meterRegistry = metricHandler.buildMetricFlow(startDatetime, endDatetime);

			resp.setContentType(CONTENT_TYPE);
			resp.setStatus(HttpServletResponse.SC_OK);

			try (OutputStream output = resp.getOutputStream()) {
				Object outputValue = filter(meterRegistry, req.getParameter("type"));
				metricHandler.getWriter(req).writeValue(output, outputValue);
			}
			catch (IOException e) {
				throw new MetricsException(e);
			}
		}
	}
	
	private Object filter(JmxMeterRegistry meterRegistry, String type) throws MetricsException {
		boolean filterByType = type != null && !type.isEmpty();
		
		if (filterByType) {
			SortedMap<String, ? extends Metric> metrics;
			
			switch (type) {
				case "gauges":
					metrics = meterRegistry.getDropwizardRegistry().getGauges();
					break;
				case "histograms":
					metrics = meterRegistry.getDropwizardRegistry().getHistograms();
					break;
				default:
					throw new MetricsException("Invalid metric type: " + type);
			}
			
			return metrics;
		}
		
		return meterRegistry;
	}
	
	public void setMetricHandler(MetricHandler metricHandler) {
		this.metricHandler = metricHandler;
	}
}
