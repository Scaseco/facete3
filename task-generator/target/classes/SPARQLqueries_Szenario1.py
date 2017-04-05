
# coding: utf-8

# # SPARQL Queries for transport dataset
# 

# ## Reading in the data

# In[1]:

import rdflib


# In[2]:

g = rdflib.Graph()


# In[3]:

g.parse("../podigg-lc_7/podigg-lc/output_data/lc.ttl", format='n3')


# ##  Scenario 1

# In[4]:

lat_lower_1 = 0.3
lat_lower_1b = 0.4 ## larger than first values
lat_lower_1c = 0.5 ## larger than last values
lat_lower_1d = 0.45 ## smaller than last values, larger than first value
lat_upper_1 = 1.7
lat_upper_1b = 1.6 ## smaller than first values
lat_upper_1c = 1.5 ## smaller than last values
lat_upper_1d = 1.55 ## larger than last values, smaller than first value
long_lower_1 = 0.3
long_lower_1b = 0.4 ## larger than first values
long_lower_1c = 0.5 ## larger than last values
long_lower_1d = 0.45 ## smaller than first values, larger than first value
long_upper_1 = 1.7
long_upper_1b = 1.6 ## smaller than first values
long_upper_1c = 1.5 ## smaller than last values
long_upper_1d = 1.55 ## larger than last value, smaller than first values
time_start_1 = "1970-01-27T01:00:00.000Z"
time_start_1b = "1970-01-28T01:00:00.000Z" ## larger than first values
time_end_1 = "1970-02-03T01:00:00.000Z"
time_end_1b = "1970-02-01T01:00:00.000Z" ## smaller than first values
station_1 = "Notus"
station_1b = "Cannonville"


# In[5]:

count_station_1 = "Notus"
count_station_1b = "Notus"
count_route_1 = "<http://example.org/routes/1763>"
count_route_1b = "<http://example.org/routes/1763>"


# ## Query 1

# In[6]:

q1_1 = g.query("""
SELECT DISTINCT ?connection ?lat ?long
 WHERE {
  ?connection lc:departureStop ?stop .
  ?stop <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat ;
      <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long . 
  FILTER((("%f" < ?lat) && (?lat < "%f") && ("%f" < ?long) && (?long < "%f") ) )
  }
 """%( lat_lower_1,lat_upper_1, long_lower_1, long_upper_1  ) )


# In[7]:

for row in q1_1:
    print("%s %s %s" % row)


# ### Counts

# In[8]:

c1_1 = g.query("""
SELECT (COUNT(DISTINCT(?connection)) AS ?sum) 
 WHERE {
  ?connection lc:departureStop ?stop .
  ?stop <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat ;
        <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long ;
        gtfs:parentStation ?station .
  ?station rdfs:label "%s" .
  FILTER((("%f" < ?lat) && (?lat < "%f") && ("%f" < ?long) && (?long < "%f") ) )
  }
 """%(count_station_1 , lat_lower_1,lat_upper_1, long_lower_1, long_upper_1  ) )


# In[9]:

for row in c1_1:
    print("%s" % row)


# In[10]:

c2_1 = g.query("""
SELECT (COUNT(DISTINCT(?connection)) AS ?sum) 
 WHERE {
  ?connection lc:departureStop ?stop ;
          lcd:departureDelay ?delay .
  ?stop <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat ;
        <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long .
  FILTER((("%f" < ?lat) && (?lat < "%f") && ("%f" < ?long) && (?long < "%f") ) )
  }
 """%( lat_lower_1,lat_upper_1, long_lower_1, long_upper_1  ) )


# In[11]:

for row in c2_1:
    print("%s" % row)


# In[12]:

c3_1 = g.query("""
SELECT (COUNT(DISTINCT(?connection)) AS ?sum) 
 WHERE {
  ?connection lc:departureStop ?stop ;
              gtfs:route %s .
  ?stop <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat ;
        <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long .
  FILTER((("%f" < ?lat) && (?lat < "%f") && ("%f" < ?long) && (?long < "%f") ) )
  }
 """%( count_route_1 ,lat_lower_1,lat_upper_1, long_lower_1, long_upper_1  ) )


# In[13]:

for row in c3_1:
    print("%s" % row)


# ## Query 2

# In[14]:

q2_1 = g.query("""
SELECT DISTINCT ?connection ?lat ?long ?time
 WHERE {
  ?connection lc:departureStop ?stop ;
              lc:departureTime ?time . 
  ?stop <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat ;
      <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long . 
  FILTER((("%f" < ?lat) && (?lat < "%f") && ("%f" < ?long) && (?long < "%f") 
      && ("%s"^^xsd:dateTime < ?time ) ) )
  }
 """%( lat_lower_1,lat_upper_1, long_lower_1, long_upper_1 , time_start_1  ) )
   


# In[15]:

for row in q2_1:
    print("%s \t %s \t %s \t %s" % row)


# ###  Counts

# In[16]:

c4_1 = g.query("""
SELECT (COUNT( DISTINCT (?connection)) AS ?sum )
 WHERE {
  ?connection lc:departureStop ?stop .
  ?connection lc:departureTime ?time . 
  ?stop <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat ;
      <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long ;
       gtfs:parentStation ?station .
  ?station rdfs:label "%s" .
  FILTER((("%f" < ?lat) && (?lat < "%f") && ("%f" < ?long) && (?long < "%f") 
      && ("%s"^^xsd:dateTime < ?time ) ))
  }
 """%( count_station_1b ,lat_lower_1,lat_upper_1, long_lower_1, long_upper_1 , time_start_1  ) )


# In[17]:

for row in c4_1 :
    print("%s" % row)


# In[18]:

c5_1 = g.query("""
SELECT (COUNT(DISTINCT(?connection)) AS ?sum) 
 WHERE {
  ?connection lc:departureStop ?stop ;
          lcd:departureDelay ?delay .
  ?connection lc:departureTime ?time . 
  ?stop <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat ;
        <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long .
  FILTER((("%f" < ?lat) && (?lat < "%f") && ("%f" < ?long) && (?long < "%f")&& ("%s"^^xsd:dateTime < ?time ) ) )
  }
 """%( lat_lower_1,lat_upper_1, long_lower_1, long_upper_1 , time_start_1 ) )


# In[19]:

for row in c5_1 :
    print("%s" % row)


# ## Query 3

# In[20]:

q3_1 = g.query("""
SELECT DISTINCT ?connection ?lat ?long ?time
 WHERE {
  ?connection lc:departureStop ?stop ;
              lc:departureTime ?time .
  ?stop <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat ;
      <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long .
  FILTER((("%f" < ?lat) && (?lat < "%f") && ("%f" < ?long) && (?long < "%f") )
      && ("%s"^^xsd:dateTime < ?time ) && (?time < "%s"^^xsd:dateTime))
  }
 """%( lat_lower_1,lat_upper_1, long_lower_1, long_upper_1 , time_start_1 , time_end_1) )
   


# In[21]:

for row in q3_1:
    print("%s \t %s \t %s \t %s" % row)


# ### Counts

# In[22]:

c6_1 = g.query("""
SELECT (COUNT(DISTINCT(?connection)) AS ?count ) 
 WHERE {
  ?connection lc:departureStop ?stop .
  ?connection lc:departureTime ?time .
  ?connection gtfs:route %s.
  ?stop <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat ;
      <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long .
  FILTER((("%f" < ?lat) && (?lat < "%f") && ("%f" < ?long) && (?long < "%f") )
      && ("%s"^^xsd:dateTime < ?time ) && (?time < "%s"^^xsd:dateTime))
  }
 """%( count_route_1b, lat_lower_1,lat_upper_1, long_lower_1, long_upper_1 , time_start_1 , time_end_1) )


# In[23]:

for row in c6_1:
    print("%s " % row)


# ## Query 4

# In[24]:

q4_1 = g.query("""
SELECT DISTINCT ?connection ?lat ?long ?time
 WHERE {
  ?connection lc:departureStop ?stop ;
              lc:departureTime ?time .
  ?stop <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat ;
      <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long .
  FILTER((("%f" < ?lat) && (?lat < "%f") && ("%f" < ?long) && (?long < "%f") )
      && ("%s"^^xsd:dateTime < ?time ) && (?time < "%s"^^xsd:dateTime))
  }
 """%( lat_lower_1b, lat_upper_1b, long_lower_1b, long_upper_1b , time_start_1 , time_end_1) )
   


# In[25]:

for row in q4_1:
    print("%s \t %s \t %s \t %s" % row)


# ## Query 5

# In[26]:

q5_1 = g.query("""
SELECT DISTINCT ?connection ?lat ?long ?time
 WHERE {
  ?connection lc:departureStop ?stop ;
              lc:departureTime ?time .
  ?stop <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat ;
      <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long .
  FILTER((("%f" < ?lat) && (?lat < "%f") && ("%f" < ?long) && (?long < "%f") )
      && ("%s"^^xsd:dateTime < ?time ) && (?time < "%s"^^xsd:dateTime))
  }
 """%( lat_lower_1c, lat_upper_1c, long_lower_1c, long_upper_1c , time_start_1 , time_end_1) )


# In[27]:

for row in q5_1:
    print("%s \t %s \t %s \t %s" % row)


# ## Query 6

# In[28]:

q6_1 = g.query("""
SELECT DISTINCT ?connection ?lat ?long ?time
 WHERE {
  ?connection lc:departureStop ?stop ;
              lc:departureTime ?time .
  ?stop <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat ;
      <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long .
  FILTER((("%f" < ?lat) && (?lat < "%f") && ("%f" < ?long) && (?long < "%f") )
      && ("%s"^^xsd:dateTime < ?time ) && (?time < "%s"^^xsd:dateTime))
  }
 """%( lat_lower_1c, lat_upper_1c, long_lower_1c, long_upper_1c , time_start_1b , time_end_1b) )


# In[29]:

for row in q6_1:
    print("%s \t %s \t %s \t %s" % row)


# ## Query 7

# In[30]:

q7_1 = g.query("""
SELECT DISTINCT ?connection ?lat ?long ?time
 WHERE {
  ?connection lc:departureStop ?stop ;
              lc:departureTime ?time .
  ?stop <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat ;
      <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long .
  FILTER((("%f" < ?lat) && (?lat < "%f") && ("%f" < ?long) && (?long < "%f") )
      && ("%s"^^xsd:dateTime < ?time ) && (?time < "%s"^^xsd:dateTime))
  }
 """%( lat_lower_1d, lat_upper_1d, long_lower_1d, long_upper_1d , time_start_1b , time_end_1b) )


# In[31]:

for row in q7_1:
    print("%s \t %s \t %s \t %s" % row)


# ## Query 8

# In[32]:

q8_1 = g.query("""
SELECT DISTINCT ?connection ?lat ?long ?time ?station
 WHERE {
  ?connection lc:departureStop ?stop ;
              lc:departureTime ?time .
  ?stop <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat ;
      <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long ;
      gtfs:parentStation ?station .
  ?station rdfs:label "%s" .
  FILTER((("%f" < ?lat) && (?lat < "%f") && ("%f" < ?long) && (?long < "%f") 
      && ("%s"^^xsd:dateTime < ?time ) && (?time < "%s"^^xsd:dateTime)))
  }
 """%( station_1, lat_lower_1d, lat_upper_1d, long_lower_1d, long_upper_1d , time_start_1b , time_end_1b) )


# In[33]:

for row in q8_1:
    print("%s \t %s \t %s \t %s \t %s" % row)


# ## Query 9

# In[34]:

q9_1 = g.query("""
SELECT DISTINCT ?connection ?lat ?long ?time
 WHERE {
  ?connection lc:departureStop ?stop ;
              lc:departureTime ?time .
  ?stop <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat ;
      <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long .
  FILTER((("%f" < ?lat) && (?lat < "%f") && ("%f" < ?long) && (?long < "%f") )
      && ("%s"^^xsd:dateTime < ?time ) && (?time < "%s"^^xsd:dateTime))
  }
 """%( lat_lower_1d, lat_upper_1d, long_lower_1d, long_upper_1d , time_start_1b , time_end_1b) )


# In[35]:

for row in q9_1:
    print("%s \t %s \t %s \t %s " % row)


# ## Query 10

# In[36]:

q10_1 = g.query("""
SELECT DISTINCT ?connection ?lat ?long ?time ?station
 WHERE {
  ?connection lc:departureStop ?stop ;
              lc:departureTime ?time .
  ?stop <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat ;
      <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long ;
      gtfs:parentStation ?station .
  ?station rdfs:label "%s" .
  FILTER((("%f" < ?lat) && (?lat < "%f") && ("%f" < ?long) && (?long < "%f") )
      && ("%s"^^xsd:dateTime < ?time ) && (?time < "%s"^^xsd:dateTime))
  }
 """%( station_1b, lat_lower_1d, lat_upper_1d, long_lower_1d, long_upper_1d , time_start_1b , time_end_1b) )


# In[37]:

for row in q10_1:
    print("%s \t %s \t %s \t %s \t %s" % row)


# In[ ]:



