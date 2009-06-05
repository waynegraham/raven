class DocumentsController < ApplicationController
  
  helper_method :facet_fields
  
  def facet_fields
    [:collection_title_s, :collection_s, :title_s, :copy_s]
  end
  
  def index
    @response = solr.find solr_search_params
    @documents = @response.docs
    respond_to do |f|
      f.html
    end
  end
  
  def collection_search
    @response = solr.find solr_search_params.merge({'hl.fragsize'=>20})
    @documents = @response.docs
    render :layout=>false
  end
  
  def show
    @response = solr.find(:q=>"id:#{params[:id]}")
    @document = @response.docs.first
    @document.extend Raven::DocExt::TOC
  end
  
  protected
  
  def solr_search_params
    {
      :q => params[:q],
      :phrase_filters => params[:f],
      :qt => :dismax,
      :per_page => 10,
      :page => params[:page],
      :facets => {
        :fields=>facet_fields
      },
      'facet.limit' => 10,
      'facet.mincount' => 1,
      'facet.sort' => true,
      'hl' => 'on',
      'hl.fl' => '*_t',
      'hl.snippets' => 5,
      'hl.fragsize' => 300,
      #:fl => 'text',
      #:qf => 'text'
    }
  end
  
end