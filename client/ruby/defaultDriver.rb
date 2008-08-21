require 'default.rb'
require 'defaultMappingRegistry.rb'
require 'soap/rpc/driver'

class OBDQueryServicePortType < ::SOAP::RPC::Driver
  DefaultEndpointUrl = "http://localhost:8080/axis2/services/OBDQueryService"

  Methods = [
    [ "urn:getID",
      "getID",
      [ ["out", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getIDResponse"]] ],
      { :request_style =>  :document, :request_use =>  :literal,
        :response_style => :document, :response_use => :literal,
        :faults => {} }
    ],
    [ "urn:getStatementsForTarget",
      "getStatementsForTarget",
      [ ["in", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getStatementsForTarget"]],
        ["out", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getStatementsForTargetResponse"]] ],
      { :request_style =>  :document, :request_use =>  :literal,
        :response_style => :document, :response_use => :literal,
        :faults => {} }
    ],
    [ "urn:getAnnotationGraphAroundNode",
      "getAnnotationGraphAroundNode",
      [ ["in", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getAnnotationGraphAroundNode"]],
        ["out", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getAnnotationGraphAroundNodeResponse"]] ],
      { :request_style =>  :document, :request_use =>  :literal,
        :response_style => :document, :response_use => :literal,
        :faults => {} }
    ],
    [ "urn:getNodes",
      "getNodes",
      [ ["out", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getNodesResponse"]] ],
      { :request_style =>  :document, :request_use =>  :literal,
        :response_style => :document, :response_use => :literal,
        :faults => {} }
    ],
    [ "urn:getNodesWithSource",
      "getNodesWithSource",
      [ ["in", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getNodesWithSource"]],
        ["out", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getNodesWithSourceResponse"]] ],
      { :request_style =>  :document, :request_use =>  :literal,
        :response_style => :document, :response_use => :literal,
        :faults => {} }
    ],
    [ "urn:getStatementsForNode",
      "getStatementsForNode",
      [ ["in", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getStatementsForNode"]],
        ["out", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getStatementsForNodeResponse"]] ],
      { :request_style =>  :document, :request_use =>  :literal,
        :response_style => :document, :response_use => :literal,
        :faults => {} }
    ],
    [ "urn:getStatementsForTargetWithSource",
      "getStatementsForTargetWithSource",
      [ ["in", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getStatementsForTargetWithSource"]],
        ["out", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getStatementsForTargetWithSourceResponse"]] ],
      { :request_style =>  :document, :request_use =>  :literal,
        :response_style => :document, :response_use => :literal,
        :faults => {"C_Exception"=>{:encodingstyle=>"document", :ns=>"http://ws.obd.org", :name=>"Exception", :use=>"literal", :namespace=>nil}} }
    ],
    [ "urn:getNode",
      "getNode",
      [ ["in", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getNode"]],
        ["out", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getNodeResponse"]] ],
      { :request_style =>  :document, :request_use =>  :literal,
        :response_style => :document, :response_use => :literal,
        :faults => {} }
    ],
    [ "urn:getSourceNodes",
      "getSourceNodes",
      [ ["out", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getSourceNodesResponse"]] ],
      { :request_style =>  :document, :request_use =>  :literal,
        :response_style => :document, :response_use => :literal,
        :faults => {} }
    ],
    [ "urn:getStatementsForNodeWithSource",
      "getStatementsForNodeWithSource",
      [ ["in", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getStatementsForNodeWithSource"]],
        ["out", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getStatementsForNodeWithSourceResponse"]] ],
      { :request_style =>  :document, :request_use =>  :literal,
        :response_style => :document, :response_use => :literal,
        :faults => {} }
    ],
    [ "urn:getStatement",
      "getStatement",
      [ ["in", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getStatement"]],
        ["out", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getStatementResponse"]] ],
      { :request_style =>  :document, :request_use =>  :literal,
        :response_style => :document, :response_use => :literal,
        :faults => {} }
    ],
    [ "urn:getSubjectStatements",
      "getSubjectStatements",
      [ ["in", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getSubjectStatements"]],
        ["out", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getSubjectStatementsResponse"]] ],
      { :request_style =>  :document, :request_use =>  :literal,
        :response_style => :document, :response_use => :literal,
        :faults => {} }
    ],
    [ "urn:getGraph",
      "getGraph",
      [ ["out", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getGraphResponse"]] ],
      { :request_style =>  :document, :request_use =>  :literal,
        :response_style => :document, :response_use => :literal,
        :faults => {} }
    ],
    [ "urn:getSummaryStatistics",
      "getSummaryStatistics",
      [ ["out", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getSummaryStatisticsResponse"]] ],
      { :request_style =>  :document, :request_use =>  :literal,
        :response_style => :document, :response_use => :literal,
        :faults => {} }
    ],
    [ "urn:getStatementsWithSource",
      "getStatementsWithSource",
      [ ["in", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getStatementsWithSource"]],
        ["out", "parameters", ["::SOAP::SOAPElement", "http://ws.obd.org", "getStatementsWithSourceResponse"]] ],
      { :request_style =>  :document, :request_use =>  :literal,
        :response_style => :document, :response_use => :literal,
        :faults => {} }
    ]
  ]

  def initialize(endpoint_url = nil)
    endpoint_url ||= DefaultEndpointUrl
    super(endpoint_url, nil)
    self.mapping_registry = DefaultMappingRegistry::EncodedRegistry
    self.literal_mapping_registry = DefaultMappingRegistry::LiteralRegistry
    init_methods
  end

private

  def init_methods
    Methods.each do |definitions|
      opt = definitions.last
      if opt[:request_style] == :document
        add_document_operation(*definitions)
      else
        add_rpc_operation(*definitions)
        qname = definitions[0]
        name = definitions[2]
        if qname.name != name and qname.name.capitalize == name.capitalize
          ::SOAP::Mapping.define_singleton_method(self, qname.name) do |*arg|
            __send__(name, *arg)
          end
        end
      end
    end
  end
end

