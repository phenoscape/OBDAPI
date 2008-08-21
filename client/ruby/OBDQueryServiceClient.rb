#!/usr/bin/env ruby

require 'rubygems'
require_gem 'soap4r'

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

puts obj.getID

# SYNOPSIS
#   getStatementsForTarget(parameters)
#
# ARGS
#   parameters      GetStatementsForTarget - {http://ws.obd.org}getStatementsForTarget
#
# RETURNS
#   parameters      GetStatementsForTargetResponse - {http://ws.obd.org}getStatementsForTargetResponse
#
parameters = nil
puts obj.getStatementsForTarget(parameters)

# SYNOPSIS
#   getNodes
#
# ARGS
#   N/A
#
# RETURNS
#   parameters      GetNodesResponse - {http://ws.obd.org}getNodesResponse
#

puts obj.getNodes

# SYNOPSIS
#   getNodesWithSource(parameters)
#
# ARGS
#   parameters      GetNodesWithSource - {http://ws.obd.org}getNodesWithSource
#
# RETURNS
#   parameters      GetNodesWithSourceResponse - {http://ws.obd.org}getNodesWithSourceResponse
#
parameters = nil
puts obj.getNodesWithSource(parameters)

# SYNOPSIS
#   getStatementsForNode(parameters)
#
# ARGS
#   parameters      GetStatementsForNode - {http://ws.obd.org}getStatementsForNode
#
# RETURNS
#   parameters      GetStatementsForNodeResponse - {http://ws.obd.org}getStatementsForNodeResponse
#
parameters = nil
puts obj.getStatementsForNode(parameters)

# SYNOPSIS
#   getStatementsForTargetWithSource(parameters)
#
# ARGS
#   parameters      GetStatementsForTargetWithSource - {http://ws.obd.org}getStatementsForTargetWithSource
#
# RETURNS
#   parameters      GetStatementsForTargetWithSourceResponse - {http://ws.obd.org}getStatementsForTargetWithSourceResponse
#
# RAISES
#   parameters      C_Exception - {http://ws.obd.org}Exception
#
parameters = nil
puts obj.getStatementsForTargetWithSource(parameters)

# SYNOPSIS
#   getNode(parameters)
#
# ARGS
#   parameters      GetNode - {http://ws.obd.org}getNode
#
# RETURNS
#   parameters      GetNodeResponse - {http://ws.obd.org}getNodeResponse
#
parameters = nil
puts obj.getNode(parameters)

# SYNOPSIS
#   getSourceNodes
#
# ARGS
#   N/A
#
# RETURNS
#   parameters      GetSourceNodesResponse - {http://ws.obd.org}getSourceNodesResponse
#

puts obj.getSourceNodes

# SYNOPSIS
#   getStatementsForNodeWithSource(parameters)
#
# ARGS
#   parameters      GetStatementsForNodeWithSource - {http://ws.obd.org}getStatementsForNodeWithSource
#
# RETURNS
#   parameters      GetStatementsForNodeWithSourceResponse - {http://ws.obd.org}getStatementsForNodeWithSourceResponse
#
parameters = nil
puts obj.getStatementsForNodeWithSource(parameters)

# SYNOPSIS
#   getSubjectStatements(parameters)
#
# ARGS
#   parameters      GetSubjectStatements - {http://ws.obd.org}getSubjectStatements
#
# RETURNS
#   parameters      GetSubjectStatementsResponse - {http://ws.obd.org}getSubjectStatementsResponse
#
parameters = nil
puts obj.getSubjectStatements(parameters)

# SYNOPSIS
#   getStatementsWithSource(parameters)
#
# ARGS
#   parameters      GetStatementsWithSource - {http://ws.obd.org}getStatementsWithSource
#
# RETURNS
#   parameters      GetStatementsWithSourceResponse - {http://ws.obd.org}getStatementsWithSourceResponse
#
parameters = nil
puts obj.getStatementsWithSource(parameters)

# SYNOPSIS
#   getSummaryStatistics
#
# ARGS
#   N/A
#
# RETURNS
#   parameters      GetSummaryStatisticsResponse - {http://ws.obd.org}getSummaryStatisticsResponse
#

puts obj.getSummaryStatistics

# SYNOPSIS
#   getStatements
#
# ARGS
#   N/A
#
# RETURNS
#   parameters      GetStatementsResponse - {http://ws.obd.org}getStatementsResponse
#

puts obj.getStatements


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

puts obj.getID

# SYNOPSIS
#   getStatementsForTarget(parameters)
#
# ARGS
#   parameters      GetStatementsForTarget - {http://ws.obd.org}getStatementsForTarget
#
# RETURNS
#   parameters      GetStatementsForTargetResponse - {http://ws.obd.org}getStatementsForTargetResponse
#
parameters = nil
puts obj.getStatementsForTarget(parameters)

# SYNOPSIS
#   getNodes
#
# ARGS
#   N/A
#
# RETURNS
#   parameters      GetNodesResponse - {http://ws.obd.org}getNodesResponse
#

puts obj.getNodes

# SYNOPSIS
#   getNodesWithSource(parameters)
#
# ARGS
#   parameters      GetNodesWithSource - {http://ws.obd.org}getNodesWithSource
#
# RETURNS
#   parameters      GetNodesWithSourceResponse - {http://ws.obd.org}getNodesWithSourceResponse
#
parameters = nil
puts obj.getNodesWithSource(parameters)

# SYNOPSIS
#   getStatementsForNode(parameters)
#
# ARGS
#   parameters      GetStatementsForNode - {http://ws.obd.org}getStatementsForNode
#
# RETURNS
#   parameters      GetStatementsForNodeResponse - {http://ws.obd.org}getStatementsForNodeResponse
#
parameters = nil
puts obj.getStatementsForNode(parameters)

# SYNOPSIS
#   getStatementsForTargetWithSource(parameters)
#
# ARGS
#   parameters      GetStatementsForTargetWithSource - {http://ws.obd.org}getStatementsForTargetWithSource
#
# RETURNS
#   parameters      GetStatementsForTargetWithSourceResponse - {http://ws.obd.org}getStatementsForTargetWithSourceResponse
#
# RAISES
#   parameters      C_Exception - {http://ws.obd.org}Exception
#
parameters = nil
puts obj.getStatementsForTargetWithSource(parameters)

# SYNOPSIS
#   getNode(parameters)
#
# ARGS
#   parameters      GetNode - {http://ws.obd.org}getNode
#
# RETURNS
#   parameters      GetNodeResponse - {http://ws.obd.org}getNodeResponse
#
parameters = nil
puts obj.getNode(parameters)

# SYNOPSIS
#   getSourceNodes
#
# ARGS
#   N/A
#
# RETURNS
#   parameters      GetSourceNodesResponse - {http://ws.obd.org}getSourceNodesResponse
#

puts obj.getSourceNodes

# SYNOPSIS
#   getStatementsForNodeWithSource(parameters)
#
# ARGS
#   parameters      GetStatementsForNodeWithSource - {http://ws.obd.org}getStatementsForNodeWithSource
#
# RETURNS
#   parameters      GetStatementsForNodeWithSourceResponse - {http://ws.obd.org}getStatementsForNodeWithSourceResponse
#
parameters = nil
puts obj.getStatementsForNodeWithSource(parameters)

# SYNOPSIS
#   getSubjectStatements(parameters)
#
# ARGS
#   parameters      GetSubjectStatements - {http://ws.obd.org}getSubjectStatements
#
# RETURNS
#   parameters      GetSubjectStatementsResponse - {http://ws.obd.org}getSubjectStatementsResponse
#
parameters = nil
puts obj.getSubjectStatements(parameters)

# SYNOPSIS
#   getStatementsWithSource(parameters)
#
# ARGS
#   parameters      GetStatementsWithSource - {http://ws.obd.org}getStatementsWithSource
#
# RETURNS
#   parameters      GetStatementsWithSourceResponse - {http://ws.obd.org}getStatementsWithSourceResponse
#
parameters = nil
puts obj.getStatementsWithSource(parameters)

# SYNOPSIS
#   getSummaryStatistics
#
# ARGS
#   N/A
#
# RETURNS
#   parameters      GetSummaryStatisticsResponse - {http://ws.obd.org}getSummaryStatisticsResponse
#

puts obj.getSummaryStatistics

# SYNOPSIS
#   getStatements
#
# ARGS
#   N/A
#
# RETURNS
#   parameters      GetStatementsResponse - {http://ws.obd.org}getStatementsResponse
#

puts obj.getStatements


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

puts obj.getID

# SYNOPSIS
#   getStatementsForTarget(parameters)
#
# ARGS
#   parameters      GetStatementsForTarget - {http://ws.obd.org}getStatementsForTarget
#
# RETURNS
#   parameters      GetStatementsForTargetResponse - {http://ws.obd.org}getStatementsForTargetResponse
#
parameters = nil
puts obj.getStatementsForTarget(parameters)

# SYNOPSIS
#   getNodes
#
# ARGS
#   N/A
#
# RETURNS
#   parameters      GetNodesResponse - {http://ws.obd.org}getNodesResponse
#

puts obj.getNodes

# SYNOPSIS
#   getNodesWithSource(parameters)
#
# ARGS
#   parameters      GetNodesWithSource - {http://ws.obd.org}getNodesWithSource
#
# RETURNS
#   parameters      GetNodesWithSourceResponse - {http://ws.obd.org}getNodesWithSourceResponse
#
parameters = nil
puts obj.getNodesWithSource(parameters)

# SYNOPSIS
#   getStatementsForNode(parameters)
#
# ARGS
#   parameters      GetStatementsForNode - {http://ws.obd.org}getStatementsForNode
#
# RETURNS
#   parameters      GetStatementsForNodeResponse - {http://ws.obd.org}getStatementsForNodeResponse
#
parameters = nil
puts obj.getStatementsForNode(parameters)

# SYNOPSIS
#   getStatementsForTargetWithSource(parameters)
#
# ARGS
#   parameters      GetStatementsForTargetWithSource - {http://ws.obd.org}getStatementsForTargetWithSource
#
# RETURNS
#   parameters      GetStatementsForTargetWithSourceResponse - {http://ws.obd.org}getStatementsForTargetWithSourceResponse
#
# RAISES
#   parameters      C_Exception - {http://ws.obd.org}Exception
#
parameters = nil
puts obj.getStatementsForTargetWithSource(parameters)

# SYNOPSIS
#   getNode(parameters)
#
# ARGS
#   parameters      GetNode - {http://ws.obd.org}getNode
#
# RETURNS
#   parameters      GetNodeResponse - {http://ws.obd.org}getNodeResponse
#
parameters = nil
puts obj.getNode(parameters)

# SYNOPSIS
#   getSourceNodes
#
# ARGS
#   N/A
#
# RETURNS
#   parameters      GetSourceNodesResponse - {http://ws.obd.org}getSourceNodesResponse
#

puts obj.getSourceNodes

# SYNOPSIS
#   getStatementsForNodeWithSource(parameters)
#
# ARGS
#   parameters      GetStatementsForNodeWithSource - {http://ws.obd.org}getStatementsForNodeWithSource
#
# RETURNS
#   parameters      GetStatementsForNodeWithSourceResponse - {http://ws.obd.org}getStatementsForNodeWithSourceResponse
#
parameters = nil
puts obj.getStatementsForNodeWithSource(parameters)

# SYNOPSIS
#   getSubjectStatements(parameters)
#
# ARGS
#   parameters      GetSubjectStatements - {http://ws.obd.org}getSubjectStatements
#
# RETURNS
#   parameters      GetSubjectStatementsResponse - {http://ws.obd.org}getSubjectStatementsResponse
#
parameters = nil
puts obj.getSubjectStatements(parameters)

# SYNOPSIS
#   getStatementsWithSource(parameters)
#
# ARGS
#   parameters      GetStatementsWithSource - {http://ws.obd.org}getStatementsWithSource
#
# RETURNS
#   parameters      GetStatementsWithSourceResponse - {http://ws.obd.org}getStatementsWithSourceResponse
#
parameters = nil
puts obj.getStatementsWithSource(parameters)

# SYNOPSIS
#   getSummaryStatistics
#
# ARGS
#   N/A
#
# RETURNS
#   parameters      GetSummaryStatisticsResponse - {http://ws.obd.org}getSummaryStatisticsResponse
#

puts obj.getSummaryStatistics

# SYNOPSIS
#   getStatements
#
# ARGS
#   N/A
#
# RETURNS
#   parameters      GetStatementsResponse - {http://ws.obd.org}getStatementsResponse
#

puts obj.getStatements


