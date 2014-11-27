package hu.mta.sztaki.lpds.cloud.simulator.iaas.pmscheduling.newsvendor;


import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * @author Krisztián Varga
 */
public class NewsVendor {

	private HashedMap<Integer, Integer> data = new HashedMap<Integer, Integer>();
	private Integer cr;
	private Integer cv;
	private Integer penaltyCost;
	private Integer handlingCost;

	public NewsVendor(final HashedMap<Integer, Integer> data, final Integer cr, final Integer cv, final Integer penaltyCost, final Integer handlingCost) {
		this.data = data;
		this.cr = cr;
		this.cv = cv;
		this.penaltyCost = penaltyCost;
		this.handlingCost = handlingCost;
	}

	/**
	 * Calculate optimum quantity
	 * @return
	 */
	public Integer calculate() {
		Integer[] sum_e = new Integer[data.size()];
		Integer[] sum_d = new Integer[data.size()];

		int SUME=0, SUMD=0;

		MapIterator<Integer,Integer> mapIterator = data.mapIterator();
		int k=0;
		while(mapIterator.hasNext()) {
			mapIterator.next();
			SUME += mapIterator.getKey();
			SUMD += mapIterator.getValue();
			sum_e[k] = mapIterator.getKey();
			sum_d[k] = mapIterator.getValue();
			k++;
		}


		NormalDistribution normalDistribution = new NormalDistribution();

		double X = normalDistribution.sample();
		boolean talalat = false;

		int q = 0;

		while (!talalat) {

			double[] F = new double[data.size()];
			for (int i = 0; i < sum_e.length-1; i++) {
				double e_sum = 0, d_sum = 0;
				for (int j = 0; j < i + 1; j++) {
					e_sum += sum_e[j];
					d_sum += sum_d[j];

				}
				F[i] = normalDistribution.cumulativeProbability((q - e_sum) / d_sum);
			}
			double t_szamlalo = cr-cv+penaltyCost;
			double t_nevezo = cr-cv+handlingCost+penaltyCost;

			for( int i=0;i<sum_e.length-1;i++) {
				t_szamlalo -= handlingCost*F[i];
			}

			double t = t_szamlalo/t_nevezo;

			double right = 1-t;

			right = normalDistribution.inverseCumulativeProbability(right);


			double FF = -SUMD*right+SUME;

			if (q == (int)Math.floor(FF)) {
				return q;
			}

			q++;

		}

		return null;

	}
}
