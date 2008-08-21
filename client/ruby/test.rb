#!/usr/bin/env ruby

require 'rubygems'
require_gem 'soap4r'
require "pp"

require 'defaultDriver.rb'

endpoint_url = ARGV.shift
obj = OBDQueryServicePortType.new(endpoint_url)

# run ruby with -d to see SOAP wiredumps.
obj.wiredump_dev = STDERR if $DEBUG

# SYNOPSIS
#   getID
#
# ARGS
#   N/A
#
# RETURNS
#   parameters      GetIDResponse - {http://ws.obd.org}getIDResponse
#

print obj.getID

#pp obj.methods

# SYNOPSIS
#   getStatementsForTarget(parameters)
#
# ARGS
#   parameters      GetStatementsForTarget - {http://ws.obd.org}getStatementsForTarget
#
# RETURNS
#   parameters      GetStatementsForTargetResponse - {http://ws.obd.org}getStatementsForTargetResponse
#
#parameters = nil

sl = obj.getNode(:id=>"PATO:0000052")
pp sl

sl = obj.getStatementsForTarget(:id=>"PATO:0000052")
pp sl

#sl = obj.getStatementsForNode(:id=>"GO:0008151")
#pp sl

#sl2 = obj.getSubjectStatementsForNode(:targetId=>"GO:0008151")
#sl = obj.getStatementsForTarget(["b"])
#pp sl2

#sl2 = obj.getSummaryStatistics()
#sl = obj.getStatementsForTarget()
#pp sl2
