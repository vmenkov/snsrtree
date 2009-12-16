<html>

<head>
<!-- meta http-equiv=Content-Type content="text/html; charset=windows-1252" -->
<title>SNSR Software</title>
<link rel="icon" type="image/vnd.microsoft.icon" href="/dd/favicon16.ico">
</head>

<body>

<h1>SNSRTREE: Software
for optimal management of multiple sensors or tests for the presence of a
nuclear threat</h1>

<div class=Section1 align=center>

<p><strong><i><span lang=PT-BR>Paul
B. Kantor (paul.kantor@rutgers.edu) &amp; Endre Boros (boros@rutcor.rutgers.edu)</span></i></strong><br>
<i>Principal Investigators</i></p>

<p><strong><i>Vladimir Menkov</i></strong><br>
<i>Lead Programmer</i></p>
</div>


<h2>Contents</h2>
<!-- Table of contents prepared with mkToc.pl -->
<ol>
<li><a href="#1">1. Overview</a>
<ul>
<li><a href="#3.1">3.1. Mixing and Deception</a>
<li><a href="#3.2">3.2. Preserving Information to Maximize Detection</a>
<li><a href="#3.3">3.3. Stochastic Independence and Dynamic Programming</a>
</ul>
<li><a href="#demos">DNDO Demos</a>
<li><a href="#res">Web Resources</a>
<li><a href="#papers">More Papers</a>
</ol>


<div class=Section1>
<h2><a name="1">1. Overview</a></h2>


<p>This site provides a gateway to some demonstration software
and the major software package, SNSRTREE developed at Rutgers University with support from the Domestic Nuclear Detection Office of the Department of
Homeland Security under NSF Grant Number CBET-0735910 and DNDO Contract Number DHS#2008-DN-077-ARI003-02,
in the program directed by Dr. Austin Kuhn. </p>

<h2><a name="2">2. The Power of a Test or Sensor: The ROC curve</2></h2>

<p>It is well known that a real test or sensor does not give
perfect information. In fact, the way in which is fails to be perfect <i>cannot
be summarized in a single number.</i> This is because there are two
fundamentally different kinds of mistakes (false negatives, and false
positives). Their relative importance depends on how serious they are, as
mistakes, and also depends on how likely it is that there are true positives to
be detected. </p>

<p>Tests and sensors come in many types, and it is important to
have the best mathematical representation of how they perform, in order to
optimize cost, detection and other considerations. This is done using a concept
called the ROC plot or ROC curve. The ROC curve is a very important tool which
permits us to simultaneously consider all kinds of tests and sensors, which may
be radiation detectors, or X-ray imaging, or the examination of documents. </p>

<p>The basic principle underlying the ROC curve is that any
particular tool or test that we use gives us something that we will call a
"reading". This reading might be a numerical score assigned on the basis of
examining documents, or it may be the number of counts occurring in a
particular energy (frequency) window in a radiation detector. Whatever form the
reading may take, the ROC curve uses a fundamental rule from statistics called
the Neyman-Pearson (NP) Lemma. </p>

<p>The NP Lemma gives us the unique optimal way to put the
different possible readings in order, so that we are sure to consider the most
suspicious cases first. Graphically, the result of the NP Lemma is summarized
in a single curve which is called the ROC curve. </p>

<p>In the ROC curve the horizontal axis shows what fraction of
the innocent or harmless cases will be flagged. The vertical axis shows what
fraction of the harmful cases will be flagged. If flagged items are inspected
by some perfect method, the difference between the <i>y</i>-value of the curve
and the 100% level is the fraction of threats that will be missed. There is a
well-developed statistical theory surrounding the ROC curve, and we have drawn
on it in developing our dynamic programming algorithm as described below. </p>

<h2><a name="3">3. Conceptual Foundations</a></h3>

<p>The software demonstrates three very important principles:
mixing and deception; preserving information; and dynamic programming. </p>

<h3><a name="3.1">3.1. Mixing and Deception</a></h3>

<p>The first principle is that most strategies will have, for a
specific number of incoming harmless and harmful packages, some
precise cost and some precise detection rate. However, the real-world
budget is unlikely to exactly match those. This can be dealt with by
using mixed strategies, which have the inherent added value of being
deceptive. Under a mixed strategy that mixes two
policies <strong><em>P<sub>1</sub></em></strong> and
<strong><em>P<sub>2</sub></em></strong> 
in the proportions <strong><em>a</em></strong>  
and <strong><em>1-a</em></strong> , it can be
shown that the detection rate is mixed in the same proportions, and so is the
cost. Thus, if we are in a position to spend more than the cost of policy 
<strong><em>P<sub>1</sub></em></strong>
than the cost of policy <strong><em>P<sub>2</sub></em></strong> 
we can actually select a
specific intermediate point by randomly using the policies for some fraction 
<strong><em>a</em></strong> of the cases, and using the other policy
for the rest. </p>


<h4>3.1.1. Effect at Low Budgets</h4>

<p>The most striking advantage of using these mixed deceptive
strategies occurs when the budget is low, and comparable to the cost of
applying the test to everything. The first demonstration lets you experiment
with the great benefits that can be achieved by mixing in this case. While a
naive strategy would require that we test everything first and then inspect the
most suspicious ones, there are several kinds of mixing strategies. The first,
which we call simple mixing, can be applied when we have enough money to test
everything but not to inspect everything suspicious. In this case we gain some
benefits by using a mixed deceptive strategy to decide which ones to inspect. </p>

<p>However the really dramatic gains come from the realization
that we can use a mixture of the strategy "test everything and inspect the most
suspicious" with the strategy of "do not even test". This is shown in the more
complex mixing which is the third example, and can produce substantial gains in
detection at the same cost or, alternatively, if the detection level is deemed
satisfactory the strategy can be used to reduce costs.</p>

<h3><a name="3.2">3.2. Preserving Information to Maximize Detection</a></h3>

<p>The second major principle underlying our work is that when
there is more than one test, the information gained in the first test, even
though it is imperfect, lets us "tune" the second test so that we get the best
possible information gain from the second test. </p>

<p>This insight can be applied to generate a <i>very large
linear program</i>, which can be solved by the column-generation technique. In
application the available budget (and any other constraints due to equipment,
port capacity, etc.) are imposed as constraints, and the optimization problem
is solved to give maximum detection at that particular budget. For planning
purposes it is necessary to solve the large problem over and over, for
different possible budgets. </p>

<p>This strategy has been applied to generate substantial
improvements over the case where the information is not retained.</p>

<p><span lang=PT-BR>Boros, E., Fedzhora, L., Kantor, P.B.,
Saeger, K., Stroud, P. (2009). </span><i>A Large-Scale Linear Programming Model
for Finding Optimal Container Inspection Strategies</i>. Naval Research
Logistics (NRL), <b>56</b> <i>(5)</i>, 404-420. April 16, 2009. DOI
10.1002/nav.20349&nbsp;(Subscription Required).</p>

<p><strong><span lang=PT-BR style='font-weight:normal'>Boros,
E., Fedzhora, L., Kantor, P.B., Saeger, K., &amp; Stroud, P</span></strong><strong><span
lang=PT-BR style='font-weight:normal'>.</span></strong><span lang=PT-BR>
(2006).</span><a
href="http://rutcor.rutgers.edu/pub/rrr/reports2006/26_2006.pdf"
title="RUTCOR 26-2006"> Large scale LP model for finding optimal container
inspection strategies</a>. Technical Report. RUTCOR 26-2006. </p>

<h3><a name="3.3">3.3. Stochastic Independence and Dynamic Programming</a></h3>

<p>The third key feature of our work applies to the important case
of tests that are "stochastically independent". For this widely occurring
situation, it is possible to not only solve more complicated problems involving
larger number of sensors, but also to <i>simultaneously solve the problem for
every possible budget</i>. This is done using a powerful mathematical technique
called dynamic programming. </p>

<p>The details are quite technical, and we provide a link below
to a technical report. However, intuitively it can be understood this way: 
<ul>

<li>We reason backwards, from the
last sensor in the chain toward the initial description of a complete policy. 

<li>What our software does is to find
for each individual sensor, and then for all the individual sensors considered
together, the very best collection of strategies describing all possible costs.
</p>

<li>What might happen is that with
low budget we would prefer using some particular one of the sensors, while with
a higher budget we would prefer using another, and in between we might have to
use a mixture as described above in Section 3.1. 
</ul>
</p>

<p>With this information stored in the computer, our program
then asks "what if?": "What would happen if we <b><i>prefixed</i></b> <i>each
of the other possible sensors </i>in turn to this best mixture?". In other
words, supposed we used a particular sensor beforehand, and saved the
information. The computation requires great care, as we know that if we are
going to use a particular sensor in the very last step there is no benefit to applying
it in the step before. And "buying the same information twice". The program
keeps track of all these complications and the result is a <i>complete
composite curve showing the best strategies</i> that are available using
exactly two sensors. </p>

<p>The program then continues this process iteratively,
prefixing one more sensor in each step, until finally there are no sensors left
to be considered. The overall result is a grand curve of <i>detection versus
cost of operation</i>, which can be used as a basis for planning and budgeting.
</p>

<p>This tool can also be used in a very powerful kind of "what
if" formulation. If a new kind of technology is available, and we have done
some basic tests so that we know its performance, as expressed in an ROC curve,
then the SNSRTREE code can be used to determine how much it would improve the
overall cost and detection profile, if we proceed with production and purchase
of these devices. </p>

<p>One could use this tool even before a new technology is
developed (money spent). If the researchers suggesting a new type of sensor can
give a reliable estimate of the ROC it will achieve, we can compute the
marginal improvement that will result. One may also go from needs to
technology. Decision makers may describe a "desired" ROC which would provide
the needed marginal improvement, and pose that as a challenge to researchers,
who would have to develop a corresponding physical device.</p>

<p>These alternatives provide a very good way to do prudent
management of the interrelation between fundamental research, which continues
to bring us more powerful tools, and the realities of having to defend the
nation using a limited budget. </p>


<p>Goldberg, N., Word, J., Boros, E. &amp; Kantor, P. (2008). <a
href="http://rutcor.rutgers.edu/pub/rrr/reports2008/14_2008.pdf">Optimal
Sequential Inspection Strategies.</a> RUTCOR RRR-14-2008.</p>

</div>

<h2><a name="demos">DNDO Demos</a></h2>
<ol>
<li><a href="dd/index.html">Simple single-sensor demo</a>
<li><a href="dd/ff/index.html">Frontier Finder Lite</a> (a preliminary version)
</ol>

<h2><a name="res">Web Resources</a></h2>
<ul>
<li><a href="http://code.google.com/p/snsrtree/">SNSRTREE</a> on Google Code
<li><a href="https://sakai.rutgers.edu/portal/site/117506c2-abfb-49c0-0060-cfb1c77bfdff">Project archives on Sakai (members only)</a>
<li><a href="javadoc/index.html">SNSRTREE API</a>
</ul>

<h2><a name="papers">More Papers</a></h2>
<ul>
<li><a href="http://rutcor.rutgers.edu/pub/rrr/reports2007/26_2007.pdf">RRR #26-2007</a> - Deceptive Detection Methods for Optimal Security with Inadequate Budgets: the Screening Power Index
</ul>




</body>

</html>
