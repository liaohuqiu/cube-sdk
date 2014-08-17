module Jekyll
  module MultiLang
    attr_accessor :language, :name_no_language, :is_default_language, :url_no_language, :dir_source

    def process_initialize(site, base, dir, name)
      name_no_language = name.dup

      lang = ''
      # xxx.$lang.md / $lang.xxx.md
      site.config['languages'].each{ |item|
        lang_str = item + '.'
        if name_no_language.include? lang_str
          lang = item
          name_no_language.slice! lang_str
        end
      }

      !lang || lang == '' && lang = site.config['language_default']
      @language = lang
      @dir_source = dir
      @name_no_language = name_no_language
      @is_default_language = lang == site.config['language_default']

      initialize_org(site, base, dir, name)
    end

    def url_no_language
      if !@url_no_language
        process_url
      end
      @url_no_language
    end

    def process_url
      if !@url
        url = self.url_org 
        @url_no_language = url
        lang_prefix = '/' + self.language
        if !self.is_default_language && (url && url.index(lang_prefix) != 0)
          url = lang_prefix + url
        end
        @url = url
      end
      @url
    end

    def process_to_liquid(attrs = nil)
      data_for_liquid = self.to_liquid_org(attrs)
      attrs_for_lang = self.language_attributes_for_liquid || []
      attrs_for_lang.concat(%w[language is_default_language url_no_language])
      further_data = Hash[attrs_for_lang.map { |attribute|
        [attribute, send(attribute)]
      }]
      data_for_liquid.deep_merge(further_data)
    end
  end

  # Rewrite Jekyll.site
  #
  class Site
    attr_accessor :language_default, :languages, :posts_by_language, :pages_by_language, :fill_default_content
    alias :process_org :process
    def process
      self.begin_inject
      process_org
    rescue Exception => e
      print e.backtrace.join("\n")
    end

    alias :read_org :read
    def read
      read_org
      group_posts_and_pages
    end

    # Group the post by the language
    #
    def group_posts_and_pages
      lang_default = self.language_default
      langs_remain = self.languages.dup
      langs_remain.delete(lang_default)

      @posts_by_language = {}
      @pages_by_language = {}

      self.languages.dup.each { |lang|
        @posts_by_language[lang] ||= {}
        @pages_by_language[lang] ||= {}
      }

      self.posts.each {|post|
        @posts_by_language[post.language][post.url_no_language] = post
      }
      self.pages.each {|page|
        @pages_by_language[page.language][page.url_no_language] = page
      }

      if (@fill_default_content)
        self.fill_default_content(@posts, @posts_by_language, lang_default, langs_remain, Post)
        self.fill_default_content(@pages, @pages_by_language, lang_default, langs_remain, Page)
      end
    end

    def fill_default_content(contents, grouped_contents, default, targets, kclass)
      grouped_contents[default].select{|k,v| !v.data['no_fill_default_content']}.each{ |k, content|
        targets.each{|lang|
          if !grouped_contents[lang][k]
            c = kclass.new(self, @source, content.dir_source, content.name)
            c.language = lang
            c.is_default_language = false
            grouped_contents[lang][k] = c
            contents << c
          end
        }
      }
    end

    # Only when site is initialized, this plugin will be loaded
    def begin_inject
      self.update_config(self.config)
    end

    # Public: Update site config, process languages and language_default options.
    #
    def update_config(config)
      !config['languages'] && config['languages'] = []
      !config['language_default'] && config['language_default'] = config['languages'].first;

      %w[languages language_default fill_default_content].each do |opt|
        self.send("#{opt}=", config[opt])
      end
      self.config = config
    end

    alias :site_payload_org :site_payload
    def site_payload
      payload = site_payload_org
      payload.merge({
        "posts_by_language" => self.posts_by_language,
        "pages_by_language" => self.pages_by_language,
      })
    end

  end

  class Page

    include MultiLang

    LANGUAGE_ATTRIBUTES_FOR_LIQUID = %w[]

    alias :initialize_org :initialize
    def initialize(site, base, dir, name)
      process_initialize(site, base, dir, name)
    end

    alias :url_org :url
    def url
      process_url
    end

    alias :process_org :process
    def process(name)
      process_org(@name_no_language)
    end

    alias :to_liquid_org :to_liquid
    def to_liquid(attrs = nil)
      process_to_liquid(attrs)
    end

    def language_attributes_for_liquid
      LANGUAGE_ATTRIBUTES_FOR_LIQUID
    end

    def inspect
      "#<Jekyll:Page @name=#{self.name.inspect} @url=#{self.url.inspect}>"
    end

  end

  class Post
    include MultiLang

    MATCHER_WITH_LANG = /^(.+\/)*(?:.+\.)*(\d+-\d+-\d+)-(.*)(\.[^.]+)$/

      alias :initialize_org :initialize
    def initialize(site, source, dir, name)
      process_initialize(site, source, dir, name)
    end

    alias :url_org :url
    def url
      process_url
    end

    alias :process_org :process
    def process(name)
      process_org(@name_no_language)
    end

    def inspect
      "<Post: id: #{self.id} url: #{self.url} language: #{self.language}>"
    end

    # For match /blog/$lang.2014-02-14-the-blog-name.md 
    # or /blog/2014-02-14-the-blog-name.$lang.md
    #
    def self.valid?(name)
      name =~ MATCHER_WITH_LANG
    end

    def language_attributes_for_liquid

    end

    alias :to_liquid_org :to_liquid
    def to_liquid(attrs = nil)
      process_to_liquid(attrs)
    end

  end

  module Generators

    # index.$lang.html / index.html
    #
    # => /$lang/... /...
    # 
    class Pagination
      def generate(site)
        if Pager.pagination_enabled?(site)
          pages = find_template_pages(site)
          if pages && !pages.empty?
            pages.each {|page| paginate_for_language(site, page)}
          else
            Jekyll.logger.warn "Pagination:", "Pagination is enabled, but I couldn't find " +
              "an index.html page to use as the pagination template. Skipping pagination."
          end
        end
      end

      def paginate_for_language(site, the_template_page)
        lang = the_template_page.language
        all_posts = site.posts_by_language[lang] || {}
        all_posts = all_posts.values.sort { |a, b| b <=> a }
        pages = Pager.calculate_pages(all_posts, site.config['paginate'].to_i)
        (1..pages).each do |num_page|
          pager = Pager.new(site, the_template_page, num_page, all_posts, pages)
          if num_page > 1
            newpage = Page.new(site, site.source, the_template_page.dir_source, the_template_page.name)
            newpage.pager = pager
            newpage.dir = Pager.calc_paginate_path(site, the_template_page, num_page)
            site.pages << newpage
          else
            the_template_page.pager = pager
          end
        end
      end

      # Find all the cadidate pages
      #
      def find_template_pages(site)
        site.pages.dup.select do |page|
          Pager.pagination_candidate?(site.config, page)
        end.sort do |one, two|
          two.path.size <=> one.path.size
        end
      end
    end
  end

  class Pager

    # Static: Return the pagination path of the page
    #
    # site     - the Jekyll::Site object
    # the_template_page - template page
    # num_page - the pagination page number
    #
    # Returns the pagination path as a string
    def self.calc_paginate_path(site, the_template_page, num_page)
      return nil if num_page.nil?
      return the_template_page.url if num_page <= 1
      format = site.config['paginate_path']
      format = format.sub(':num', num_page.to_s)
      path = ensure_leading_slash(format)
      path
    end

    def self.calc_paginate_path_with_lang(site, the_template_page, num_page)
      path = self.calc_paginate_path(site, the_template_page, num_page)
      return nil if path.nil?
      lang_prefix = '/' + the_template_page.language
      if !the_template_page.is_default_language && (path && path.index(lang_prefix) != 0)
        path = '/' + the_template_page.language + path
      end
      path
    end

    def self.pagination_candidate?(config, page)
      page_dir = File.dirname(File.expand_path(remove_leading_slash(page.path), config['source']))
      paginate_path = remove_leading_slash(config['paginate_path'])
      paginate_path = File.expand_path(paginate_path, config['source'])
      page.basename == 'index' &&
        in_hierarchy(config['source'], page_dir, File.dirname(paginate_path))
    end

    def initialize(site, the_template_page, page, all_posts, num_pages = nil)
      @page = page
      @per_page = site.config['paginate'].to_i
      @total_pages = num_pages || Pager.calculate_pages(all_posts, @per_page)

      if @page > @total_pages
        raise RuntimeError, "page number can't be greater than total pages: #{@page} > #{@total_pages}"
      end

      init = (@page - 1) * @per_page
      offset = (init + @per_page - 1) >= all_posts.size ? all_posts.size : (init + @per_page - 1)

      @total_posts = all_posts.size
      @posts = all_posts[init..offset]
      @previous_page = @page != 1 ? @page - 1 : nil
      @previous_page_path = Pager.calc_paginate_path_with_lang(site, the_template_page, @previous_page)
      @next_page = @page != @total_pages ? @page + 1 : nil
      @next_page_path = Pager.calc_paginate_path_with_lang(site, the_template_page, @next_page)
    end
  end
end
