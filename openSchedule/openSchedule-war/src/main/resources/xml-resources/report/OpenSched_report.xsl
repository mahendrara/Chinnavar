<xsl:stylesheet version="2.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:ns2="http://www.railml.org/schemas/2011"
    xmlns:F="myfunctions"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:fn="http://www.w3.org/2005/xpath-functions">
    <xsl:output method="xml" indent="yes" escape-uri-attributes="no"/>
    
    <xsl:template match="/">
        
                <xsl:for-each select="/ns2:railml/ns2:timetable/ns2:timetablePeriods/ns2:timetablePeriod">
                    <xsl:variable name="currSched" select="@id"/>
                    <xsl:variable name="totalRunningDays" select="F:TOTALSCHEDULEDAYS(@startDate, @endDate)"/>
                    <h1>Schedule: <b><xsl:value-of select="@name"/></b></h1><br/>
                    Total Days: <xsl:value-of select="$totalRunningDays"/><br/><br/>
                    
                    <h3>Day Types:</h3><br/>
                    <xsl:for-each select="/ns2:railml/ns2:timetable/ns2:operatingPeriods/ns2:operatingPeriod[@timetablePeriodRef=$currSched]">
                        <xsl:variable name="currDay" select="@id"/>
                        <xsl:variable name="currRunningDays" select="F:RUNNINGDAYS(@bitMask)"/>
                        <xsl:variable name="serviceNbr"  select="F:SERVICESOFDAY($currDay, /ns2:railml/ns2:timetable/ns2:rosterings)"/>

                        Name: <xsl:value-of select="@name"/><br/>
                        Code: <xsl:value-of select="@code"/><br/>
                        Running days: <xsl:value-of select="$currRunningDays"/> [<xsl:value-of select="format-number($currRunningDays div $totalRunningDays, '##.00%')"/>]<br></br>
                        Number of Services: <xsl:value-of select="$serviceNbr"/><br></br>
                        Number of Trips: <xsl:value-of select="F:TRIPSOFDAY($currDay, /ns2:railml/ns2:timetable/ns2:rosterings)"/>
                        <br/>
                        <br/>
                        <xsl:if test="$serviceNbr > 0">
                            <table style="width:100%">
                                <tr>
                                    <th>Service</th>
                                    <th>Trips</th>
<!--                                    <th>Stops</th> -->
                                    <th>Running hours: From</th>
                                    <th>Running hours: To</th>
                                    <th>Running time</th>
                                    <th>Moving</th>
                                    <th>Dwelling</th>
                                </tr>
                                <xsl:for-each select="/ns2:railml/ns2:timetable/ns2:rosterings/ns2:rostering/ns2:blocks/ns2:block">
                                    <xsl:if test="F:SERVICEHASTRIPSINDAY($currDay, .)">
                                        <xsl:variable name="runnTime"  as="xs:dayTimeDuration" select="F:RUNNINGTIMEOFSERVICE(.)"/>
                                        <xsl:variable name="dwellTime"  as="xs:dayTimeDuration" select="F:DWELLTIMEOFSERVICE(.)"/>
                                        <xsl:variable name="movingTime"  as="xs:dayTimeDuration" select="$runnTime - $dwellTime"/>
                                        
                                        <tr>
                                            <td><xsl:value-of select="@name"/></td>
                                            <td><xsl:value-of select="F:TRIPSINSERVICEOFDAY($currDay, .)"/></td>
<!--                                            <td>c</td> -->
                                            <td><xsl:value-of select="format-time(F:STARTTIMEOFSERVICE(.), '[H01]:[m01]:[s01]')"/></td>
                                            <td><xsl:value-of select="format-time(F:STOPTIMEOFSERVICE(.), '[H01]:[m01]:[s01]')"/></td>
                                            <td><xsl:value-of select="F:DAYTIMEDURATIONSTRING($runnTime)"/></td>
                                            <td><xsl:value-of select="F:DAYTIMEDURATIONSTRING($movingTime)"/></td>
                                            <td><xsl:value-of select="F:DAYTIMEDURATIONSTRING($dwellTime)"/></td>
                                        </tr>
                                    </xsl:if>                                
                                </xsl:for-each>
                            </table>
                        </xsl:if>
                        <br/>
                    </xsl:for-each>
                </xsl:for-each>
    </xsl:template>
    
    <!-- RECURSIVE, DONT CALL DIRECTLY -->
    <xsl:function name="F:_DWELLTIMEOFOCPSTT">
        <xsl:param name="ocpTT"/> <!-- this is ocpTT item -->
        <xsl:param name="time"/>
        
        <xsl:if test="$ocpTT">
            <xsl:if test="$ocpTT/ns2:times[1]/@arrival and $ocpTT/ns2:times[1]/@departure">
                <!-- my time -->
                <xsl:variable name="myDiff" as="xs:dayTimeDuration" select="F:TIMEDIFFERENCE($ocpTT/ns2:times[1]/@arrival, $ocpTT/ns2:times[1]/@arrivalDay, $ocpTT/ns2:times[1]/@departure, $ocpTT/ns2:times[1]/@departureDay)"/>
                <!-- sibling -->
                <xsl:variable name="subTime" as="xs:dayTimeDuration" select="F:_DWELLTIMEOFOCPSTT($ocpTT/following-sibling::ns2:ocpTT[1], $time)"/>
                <xsl:value-of select="$myDiff + $subTime"/>
            </xsl:if>            
            <xsl:if test="not ($ocpTT/ns2:times[1]/@arrival) or not ($ocpTT/ns2:times[1]/@departure)">
                <!-- sibling -->
                <xsl:variable name="subTime" as="xs:dayTimeDuration" select="F:_DWELLTIMEOFOCPSTT($ocpTT/following-sibling::ns2:ocpTT[1], $time)"/>
                <xsl:value-of select="$subTime"/>
            </xsl:if>            
        </xsl:if>
        <xsl:if test="not ($ocpTT)"> 
            <xsl:value-of select="$time"/>
        </xsl:if>    
        
    </xsl:function>
    <!-- RECURSIVE, DONT CALL DIRECTLY -->
    <xsl:function name="F:_DWELLTIMEOFBLOCKPARTSEQUENCE">
        <xsl:param name="blockPartSequenceItem"/> <!-- this is service item -->
        <xsl:param name="time"/>
        
        <xsl:if test="$blockPartSequenceItem"> 
            
            <!--subs -->
            <xsl:variable name="tpRef" select="$blockPartSequenceItem/ancestor::ns2:rostering/ns2:blockParts/ns2:blockPart[@id = $blockPartSequenceItem/ns2:blockPartRef/@ref]/@trainPartRef"/>
            <xsl:variable name="ocpTT" select="$blockPartSequenceItem/ancestor::ns2:timetable/ns2:trainParts/ns2:trainPart[@id = $tpRef]/ns2:ocpsTT/ns2:ocpTT[1]"/> <!-- Expect only one time -->
            <xsl:variable name="subTime" as="xs:dayTimeDuration" select="F:_DWELLTIMEOFOCPSTT($ocpTT, xs:dayTimeDuration('PT0S'))"/>
            
            
            <!-- sibling -->
            <xsl:value-of select="F:_DWELLTIMEOFBLOCKPARTSEQUENCE($blockPartSequenceItem/following-sibling::ns2:blockPartSequence[1], $time+$subTime)"/>
            
        </xsl:if>
        
        
        <xsl:if test="not ($blockPartSequenceItem)"> 
            <xsl:value-of select="$time"/>
        </xsl:if>    
    </xsl:function>
    
    <xsl:function name="F:DWELLTIMEOFSERVICE">
        <xsl:param name="blockItem"/> <!-- this is service item -->
        
        <xsl:variable name="dur" select="xs:dayTimeDuration('PT0S')"/>
        <xsl:value-of select="F:_DWELLTIMEOFBLOCKPARTSEQUENCE($blockItem/ns2:blockPartSequence[1], $dur)"/>
        
    </xsl:function>

    <xsl:function name="F:RUNNINGTIMEOFSERVICE">
        <xsl:param name="blockItem"/> <!-- this is service item -->
        
        <xsl:variable name="tpRef" select="$blockItem/ancestor::ns2:rostering/ns2:blockParts/ns2:blockPart[@id = $blockItem/ns2:blockPartSequence[1]/ns2:blockPartRef/@ref]/@trainPartRef"/>        
        <xsl:variable name="fromTime" select="$blockItem/ancestor::ns2:timetable/ns2:trainParts/ns2:trainPart[@id = $tpRef]/ns2:ocpsTT/ns2:ocpTT[1]/ns2:times[1]"/> <!-- Expect only one time -->
        
        <xsl:variable name="tpRef" select="$blockItem/ancestor::ns2:rostering/ns2:blockParts/ns2:blockPart[@id = $blockItem/ns2:blockPartSequence[last()]/ns2:blockPartRef/@ref]/@trainPartRef"/>        
        <xsl:variable name="toTime" select="$blockItem/ancestor::ns2:timetable/ns2:trainParts/ns2:trainPart[@id = $tpRef]/ns2:ocpsTT/ns2:ocpTT[last()]/ns2:times[1]"/> <!-- Expect only one time -->

        <xsl:value-of select="F:TIMEDIFFERENCE($fromTime/@departure, $fromTime/@departureDay, $toTime/@arrival, $toTime/@arrivalDay)"></xsl:value-of>
    </xsl:function>
    
    <!-- Returns first time (movement) declared for service -->
    <xsl:function name="F:STARTTIMEOFSERVICE">
        <xsl:param name="blockItem"/> <!-- this is service item -->
        <xsl:variable name="tpRef" select="$blockItem/ancestor::ns2:rostering/ns2:blockParts/ns2:blockPart[@id = $blockItem/ns2:blockPartSequence[1]/ns2:blockPartRef/@ref]/@trainPartRef"/>        
        <xsl:value-of select="$blockItem/ancestor::ns2:timetable/ns2:trainParts/ns2:trainPart[@id = $tpRef]/ns2:ocpsTT/ns2:ocpTT[1]/ns2:times/@departure"></xsl:value-of>
    </xsl:function>
    <!-- Returns last time (movement) declared for service -->
    <xsl:function name="F:STOPTIMEOFSERVICE">
        <xsl:param name="blockItem"/> <!-- this is service item -->
        <xsl:variable name="tpRef" select="$blockItem/ancestor::ns2:rostering/ns2:blockParts/ns2:blockPart[@id = $blockItem/ns2:blockPartSequence[last()]/ns2:blockPartRef/@ref]/@trainPartRef"/>        
        <xsl:value-of select="$blockItem/ancestor::ns2:timetable/ns2:trainParts/ns2:trainPart[@id = $tpRef]/ns2:ocpsTT/ns2:ocpTT[last()]/ns2:times/@arrival"></xsl:value-of>
    </xsl:function>
    
    <!-- Check if service has trips in day -->
    <xsl:function name="F:SERVICEHASTRIPSINDAY">
        <xsl:param name="opRef"/>
        <xsl:param name="blockItem"/> <!-- this is service item -->
        
        <xsl:if test="F:TRIPSINSERVICEOFDAY($opRef, $blockItem) > 0">
            <xsl:value-of select="1"/>
        </xsl:if>
    </xsl:function>
    
    <!-- Calculate dwell time on one stop -->
    <xsl:function name="F:TIMEDIFFERENCE">
        <xsl:param name="from"/>
        <xsl:param name="fromDay"/>
        <xsl:param name="to"/>
        <xsl:param name="toDay"/>
        
        <xsl:variable name="fromDate" as="xs:dateTime" select="fn:dateTime(xs:date('0001-01-01'), xs:time($from)) + $fromDay*xs:dayTimeDuration('P1D')"/>
        <xsl:variable name="toDate" as="xs:dateTime" select="fn:dateTime(xs:date('0001-01-01'), xs:time($to)) + $toDay*xs:dayTimeDuration('P1D')"/>
        
        
        <xsl:value-of select="$toDate - $fromDate"/>
        
    </xsl:function>
    
    <!-- Returns number of trips in given day for service. RECURSIVE!! DONT CALL DIRECTLY FROM XSL:TEMPLATE -->
    <xsl:function name="F:BLOCKPARTSEQ" as="xs:integer">
        <xsl:param name="opRef"/>
        <xsl:param name="currItem"/>
        <xsl:param name="sum"/>

        <xsl:if test="$currItem"> 
            <xsl:variable name="tmp" select="$currItem/ns2:blockPartRef/@ref"/>
            <xsl:variable name="tripCnt" select="count($currItem/ancestor::ns2:rosterings[1]/ns2:rostering/ns2:blockParts/ns2:blockPart[@id = $tmp and @operatingPeriodRef = $opRef])"/>
            <xsl:variable name="tripCnt2" select="$tripCnt + 1"/> 
            <xsl:value-of select="F:BLOCKPARTSEQ($opRef, $currItem/following-sibling::ns2:blockPartSequence[1], $tripCnt + $sum)"/>
        </xsl:if>
        <xsl:if test="not ($currItem)"> 
            <xsl:value-of select="$sum"/>
        </xsl:if>
    </xsl:function>

    <!-- Returns number of services, which have trips in given day. RECURSIVE!! DONT CALL DIRECTLY FROM XSL:TEMPLATE -->
    <xsl:function name="F:BLOCK">
        <xsl:param name="opRef"/>
        <xsl:param name="blockItem"/>
        <xsl:param name="sum"/>
        
        <xsl:if test="$blockItem">
            <!--subs -->
            <xsl:variable name="tripCnt" select="F:BLOCKPARTSEQ($opRef, $blockItem/ns2:blockPartSequence[1], 0)"/>
            <xsl:if test="$tripCnt > 0">
                <xsl:value-of select="F:BLOCK($opRef, $blockItem/following-sibling::ns2:block[1], $sum+1)"/>
            </xsl:if>
            <xsl:if test="not ($tripCnt > 0)">
                <xsl:value-of select="F:BLOCK($opRef, $blockItem/following-sibling::ns2:block[1], $sum)"/>
            </xsl:if>
        </xsl:if>
        <xsl:if test="not ($blockItem)"> 
            <xsl:value-of select="$sum"/>
        </xsl:if>        
    </xsl:function>


    <!-- Returns number of trips, in given day. RECURSIVE!! DONT CALL DIRECTLY FROM XSL:TEMPLATE -->
    <xsl:function name="F:BLOCK2">
        <xsl:param name="opRef"/>
        <xsl:param name="blockItem"/>
        <xsl:param name="sum"/>
        
        <xsl:if test="$blockItem"> 
            <!--subs -->
            <xsl:variable name="tripCnt" select="F:BLOCKPARTSEQ($opRef, $blockItem/ns2:blockPartSequence[1], 0)"/>
            <xsl:value-of select="F:BLOCK2($opRef, $blockItem/following-sibling::ns2:block[1], $sum+$tripCnt)"/>
        </xsl:if>
        <xsl:if test="not ($blockItem)"> 
            <xsl:value-of select="$sum"/>
        </xsl:if>        
    </xsl:function>
    
    <!-- Returns Number of trips in given service for given day -->
    <xsl:function name="F:TRIPSINSERVICEOFDAY">
        <xsl:param name="opRef"/>
        <xsl:param name="blockItem"/>
        
        <xsl:value-of select="F:BLOCKPARTSEQ($opRef, $blockItem/ns2:blockPartSequence[1], 0)"/>
        
    </xsl:function>
    
    <!-- Returns number of services, which have trips in given day -->
    <xsl:function name="F:SERVICESOFDAY">
        <xsl:param name="opRef"/>
        <xsl:param name="rosterings"/>
        
        <xsl:value-of select="F:BLOCK($opRef, $rosterings/ns2:rostering/ns2:blocks/ns2:block[1],0)"/>
    </xsl:function>

    <!-- Returns number of trips in all services in given day -->
    <xsl:function name="F:TRIPSOFDAY">
        <xsl:param name="opRef"/>
        <xsl:param name="rosterings"/>
        <xsl:value-of select="F:BLOCK2($opRef, $rosterings/ns2:rostering/ns2:blocks/ns2:block[1],0)"/>
    </xsl:function>
    
    <xsl:function name="F:TOTALSCHEDULEDAYS">
        <xsl:param name="beginDay"></xsl:param>
        <xsl:param name="endDay"></xsl:param>
        <xsl:variable name="startDate" as="xs:date" select="xs:date($beginDay)"></xsl:variable>
        <xsl:variable name="endDate" as="xs:date" select="xs:date($endDay)"></xsl:variable>
        <xsl:value-of select="fn:days-from-duration($endDate - $startDate) + 1"/>
    </xsl:function>
    <xsl:function name="F:RUNNINGDAYS">
        <xsl:param name="bitMask"></xsl:param>
        <xsl:value-of select="fn:string-length(fn:translate($bitMask, '0', ''))"/>
    </xsl:function>
    
    <xsl:function name="F:DAYTIMEDURATIONSTRING">
        <xsl:param name="duration"></xsl:param>
        
        <xsl:variable name="days" select="days-from-duration($duration)"/>
        <xsl:variable name="hours" select="hours-from-duration($duration)"/>
        <xsl:variable name="mins" select="minutes-from-duration($duration)"/>
        <xsl:variable name="secs" select="seconds-from-duration($duration)"/>

        <xsl:if test="$days>0">
            <xsl:value-of select="concat(concat($days, 'd'), concat(concat($hours, 'h'), concat(concat($mins, 'm'), concat($secs, 's'))))"/>
        </xsl:if>
        <xsl:if test="$days=0"> 
            <xsl:value-of select="concat(concat($hours, 'h'), concat(concat($mins, 'm'), concat($secs, 's')))"/>
        </xsl:if>
    </xsl:function>
</xsl:stylesheet>